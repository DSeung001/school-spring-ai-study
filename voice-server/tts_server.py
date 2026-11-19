from io import BytesIO
import re
from threading import Lock

import numpy as np
from fastapi import FastAPI, HTTPException, Response
from pydantic import BaseModel, Field
from scipy.io import wavfile
import torch
from transformers import AutoTokenizer, VitsModel
import uroman as ur

app = FastAPI()
model_id = "facebook/mms-tts-kor"
tokenizer = AutoTokenizer.from_pretrained(model_id)
# 로마자 변환을 아래에서 명시적으로 수행하므로 중복 전처리를 끈다.
tokenizer.is_uroman = False
model = VitsModel.from_pretrained(model_id).eval()
romanizer = ur.Uroman()
lock = Lock()

class SpeechRequest(BaseModel):
    text: str = Field(min_length=1, max_length=1000)

@app.get("/health")
def health():
    return {"status": "ready", "model": model_id}

@app.post("/tts")
def tts(request: SpeechRequest):
    text = re.sub(r"\[notice-\d+\]", "", request.text)
    text = re.sub(r"[*#`]", "", text).strip()
    if not text:
        raise HTTPException(status_code=400, detail="읽을 내용을 입력해 주세요.")
    waves = []
    # 긴 답변을 한 번에 합성하지 않고 짧게 나누어 처리한다.
    with lock, torch.inference_mode():
        for offset in range(0, len(text), 140):
            roman = romanizer.romanize_string(text[offset:offset + 140], lcode="kor")
            inputs = tokenizer(roman, return_tensors="pt")
            if inputs["input_ids"].shape[1] == 0:
                raise HTTPException(status_code=400, detail="합성 가능한 문자가 없습니다.")
            wave = model(**inputs).waveform[0].cpu().numpy()
            waves.append(wave)
            waves.append(np.zeros(int(model.config.sampling_rate * 0.15), dtype=np.float32))
    waveform = np.concatenate(waves)
    pcm = (np.clip(waveform, -1.0, 1.0) * 32767).astype(np.int16)
    output = BytesIO()
    wavfile.write(output, model.config.sampling_rate, pcm)
    return Response(content=output.getvalue(), media_type="audio/wav")
