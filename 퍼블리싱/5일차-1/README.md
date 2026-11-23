# 5일차-1: 답변 듣기·오디오 재생

교안: `5일차 포트폴리오화.md` → **3. 응답에 TTS 연결**.

## 변경 목적

답변 듣기·오디오 재생 기능을 적용하는 완성 HTML·JS입니다.

## 적용 준비

RAG API와 POST /api/voice/tts 구현 완료. 05-tts-portfolio에서 5일차 2절까지 진행하거나 06-result의 백엔드를 사용합니다. Ollama와 TTS 서버도 준비합니다.

적용 방법은 [퍼블리싱 안내](../README.md#적용-방법)를 따릅니다. 두 파일을 함께 복사하세요.

## 이전 단계와 비교

### `index.html`

```diff
--- 4일차-1/index.html
+++ 5일차-1/index.html
@@ -19,9 +19,12 @@
     <textarea id="content" maxlength="200" required></textarea>
     <button id="send" type="submit">질문 보내기</button>
   </form>
+  <section aria-label="음성 출력">
+    <p>답변의 듣기 버튼을 누르면 음성을 만듭니다.</p>
+    <audio id="player" controls></audio>
+  </section>
   <p id="status" role="status" aria-live="polite"></p>
   <ul id="questions"></ul>
-  <!-- static 폴더 파일은 Spring이 그대로 내려 줌. 이 파일은 http://localhost:8080/, app.js는 /app.js로 열림 -->
   <script src="/app.js" defer></script>
 </body>
 </html>
```

### `app.js`

```diff
--- 4일차-1/app.js
+++ 5일차-1/app.js
@@ -58,6 +58,10 @@
       } catch (error) { statusBox.textContent = error.message; }
     });
     item.append(edit, remove);
+    const speak = document.createElement('button');
+    speak.textContent = '답변 듣기';
+    speak.addEventListener('click', () => playAnswer(question.answer, speak));
+    item.append(speak);
     list.append(item);
   }
 }
@@ -75,3 +79,40 @@
   finally { send.disabled = false; }
 });
 loadQuestions().catch(error => { statusBox.textContent = error.message; });
+
+const player = document.querySelector('#player');
+let audioUrl = null;
+async function playAnswer(text, button) {
+  if (text.length > 1000) {
+    statusBox.textContent = '답변이 1000자를 넘어 읽을 수 없습니다. 더 짧은 답변으로 질문해 주세요.';
+    return;
+  }
+  button.disabled = true;
+  statusBox.textContent = '음성을 만들고 있습니다.';
+  try {
+    const response = await fetch('/api/voice/tts', {
+      method: 'POST', headers: { 'Content-Type': 'application/json' },
+      body: JSON.stringify({ text })
+    });
+    if (!response.ok) {
+      const error = await response.json();
+      throw new Error(error.message || '음성 생성 실패');
+    }
+    const blob = await response.blob();
+    player.pause();
+    if (audioUrl) URL.revokeObjectURL(audioUrl);
+    audioUrl = URL.createObjectURL(blob);
+    player.src = audioUrl;
+    try {
+      await player.play();
+      statusBox.textContent = '답변을 재생합니다.';
+    } catch {
+      statusBox.textContent = '아래 오디오 재생 버튼을 눌러 들어 주세요.';
+    }
+  } catch (error) { statusBox.textContent = error.message; }
+  finally { button.disabled = false; }
+}
+player.addEventListener('error', () => {
+  statusBox.textContent = '오디오를 재생하지 못했습니다. 다시 생성하거나 브라우저 설정을 확인해 주세요.';
+});
+window.addEventListener('beforeunload', () => { if (audioUrl) URL.revokeObjectURL(audioUrl); });
```

## 적용 후 전체 코드

### `index.html`

```html
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>학교 안내 도우미</title>
  <style>
    body { max-width: 760px; margin: 32px auto; padding: 16px; font-family: sans-serif; }
    textarea { width: 100%; min-height: 70px; }
    li { border-bottom: 1px solid #bbb; padding: 16px 0; white-space: pre-wrap; }
    button { margin: 4px; padding: 8px; }
    #status { min-height: 24px; }
  </style>
</head>
<body>
  <h1>학교 안내 도우미</h1>
  <form id="question-form">
    <label for="content">질문 (1~200자)</label>
    <textarea id="content" maxlength="200" required></textarea>
    <button id="send" type="submit">질문 보내기</button>
  </form>
  <section aria-label="음성 출력">
    <p>답변의 듣기 버튼을 누르면 음성을 만듭니다.</p>
    <audio id="player" controls></audio>
  </section>
  <p id="status" role="status" aria-live="polite"></p>
  <ul id="questions"></ul>
  <script src="/app.js" defer></script>
</body>
</html>
```

### `app.js`

```javascript
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
```

## 실행 확인

답변 듣기로 음성 요청·재생을 확인합니다. TTS 서버 중단 시 오류와 버튼 복구, 1000자 초과 시 요청 차단, 자동 재생 거절 시 수동 재생 안내를 확인합니다.

외부 AI·음성 결과는 실제 서버 연결 후 별도로 확인합니다.
