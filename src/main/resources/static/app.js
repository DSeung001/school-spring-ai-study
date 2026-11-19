const form = document.querySelector('#question-form');
const input = document.querySelector('#content');
const list = document.querySelector('#questions');
const statusBox = document.querySelector('#status');
const send = document.querySelector('#send');

async function request(url, options = {}) {
  const response = await fetch(url, options);
  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; }
  catch { throw new Error(`서버 응답을 읽을 수 없습니다. HTTP ${response.status}`); }
  if (!response.ok) throw new Error(data?.message || `HTTP ${response.status}`);
  return data;
}
function jsonOptions(method, content) {
  return { method, headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }) };
}
async function loadQuestions() {
  const questions = await request('/api/questions');
  list.replaceChildren();
  for (const question of questions) {
    const item = document.createElement('li');
    const text = document.createElement('p');
    text.textContent = `#${question.id} 질문: ${question.content}\n답변: ${question.answer}`;
    item.append(text);
    const sources = document.createElement('p');
    sources.textContent = question.sources.length
      ? '검색 근거: ' + question.sources.map(s => `${s.title} (${s.chunkId})\n${s.text}`).join('\n')
      : '검색 근거 없음';
    item.append(sources);
    const edit = document.createElement('button');
    edit.textContent = '수정';
    edit.addEventListener('click', async () => {
      const content = prompt('수정할 질문 (1~200자)', question.content);
      if (content === null) return;
      if (!content.trim() || content.length > 200) {
        statusBox.textContent = '질문은 공백을 제외해 입력하고 200자 이내로 적어 주세요.';
        return;
      }
      edit.disabled = true;
      try {
        await request(`/api/questions/${question.id}`, jsonOptions('PUT', content));
        await loadQuestions();
        statusBox.textContent = '질문과 답변을 갱신했습니다.';
      } catch (error) { statusBox.textContent = error.message; }
      finally { edit.disabled = false; }
    });
    const remove = document.createElement('button');
    remove.textContent = '삭제';
    remove.addEventListener('click', async () => {
      if (!confirm('이 질문을 삭제할까요?')) return;
      try {
        await request(`/api/questions/${question.id}`, { method: 'DELETE' });
        await loadQuestions();
        statusBox.textContent = '삭제했습니다.';
      } catch (error) { statusBox.textContent = error.message; }
    });
    item.append(edit, remove);
    const speak = document.createElement('button');
    speak.textContent = '답변 듣기';
    speak.addEventListener('click', () => playAnswer(question.answer, speak));
    item.append(speak);
    list.append(item);
  }
}
form.addEventListener('submit', async event => {
  event.preventDefault();
  if (!input.value.trim()) { statusBox.textContent = '질문을 입력해 주세요.'; return; }
  send.disabled = true;
  statusBox.textContent = '답변을 기다리고 있습니다.';
  try {
    await request('/api/questions', jsonOptions('POST', input.value));
    input.value = '';
    await loadQuestions();
    statusBox.textContent = '등록했습니다.';
  } catch (error) { statusBox.textContent = error.message; }
  finally { send.disabled = false; }
});
loadQuestions().catch(error => { statusBox.textContent = error.message; });

const player = document.querySelector('#player');
let audioUrl = null;
async function playAnswer(text, button) {
  if (text.length > 1000) {
    statusBox.textContent = '답변이 1000자를 넘어 읽을 수 없습니다. 더 짧은 답변으로 질문해 주세요.';
    return;
  }
  button.disabled = true;
  statusBox.textContent = '음성을 만들고 있습니다.';
  try {
    const response = await fetch('/api/voice/tts', {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text })
    });
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || '음성 생성 실패');
    }
    const blob = await response.blob();
    player.pause();
    if (audioUrl) URL.revokeObjectURL(audioUrl);
    audioUrl = URL.createObjectURL(blob);
    player.src = audioUrl;
    try {
      await player.play();
      statusBox.textContent = '답변을 재생합니다.';
    } catch {
      statusBox.textContent = '아래 오디오 재생 버튼을 눌러 들어 주세요.';
    }
  } catch (error) { statusBox.textContent = error.message; }
  finally { button.disabled = false; }
}
player.addEventListener('error', () => {
  statusBox.textContent = '오디오를 재생하지 못했습니다. 다시 생성하거나 브라우저 설정을 확인해 주세요.';
});
window.addEventListener('beforeunload', () => { if (audioUrl) URL.revokeObjectURL(audioUrl); });
