# 2일차-1: 질문 등록·목록 조회

교안: `2일차 스프링 MVC와 화면.md` → **4. HTML 화면과 API 연결**.

## 변경 목적

질문 등록·목록 조회 기능을 적용하는 완성 HTML·JS입니다.

## 적용 준비

GET·POST /api/questions 구현 완료. 02-spring-mvc-crud에서 2일차 3절까지 진행하거나 03-spring-ai의 백엔드를 사용합니다.

적용 방법은 [퍼블리싱 안내](../README.md#적용-방법)를 따릅니다. 두 파일을 함께 복사하세요.

## 이전 단계와 비교

### `index.html`

```diff
--- /dev/null
+++ 2일차-1/index.html
@@ -0,0 +1,27 @@
+<!doctype html>
+<html lang="ko">
+<head>
+  <meta charset="UTF-8">
+  <meta name="viewport" content="width=device-width, initial-scale=1">
+  <title>학교 안내 도우미</title>
+  <style>
+    body { max-width: 760px; margin: 32px auto; padding: 16px; font-family: sans-serif; }
+    textarea { width: 100%; min-height: 70px; }
+    li { border-bottom: 1px solid #bbb; padding: 16px 0; white-space: pre-wrap; }
+    button { margin: 4px; padding: 8px; }
+    #status { min-height: 24px; }
+  </style>
+</head>
+<body>
+  <h1>학교 안내 도우미</h1>
+  <form id="question-form">
+    <label for="content">질문 (1~200자)</label>
+    <textarea id="content" maxlength="200" required></textarea>
+    <button id="send" type="submit">질문 보내기</button>
+  </form>
+  <p id="status" role="status" aria-live="polite"></p>
+  <ul id="questions"></ul>
+  <!-- static 폴더 파일은 Spring이 그대로 내려 줌. 이 파일은 http://localhost:8080/, app.js는 /app.js로 열림 -->
+  <script src="/app.js" defer></script>
+</body>
+</html>
```

### `app.js`

```diff
--- /dev/null
+++ 2일차-1/app.js
@@ -0,0 +1,46 @@
+const form = document.querySelector('#question-form');
+const input = document.querySelector('#content');
+const list = document.querySelector('#questions');
+const statusBox = document.querySelector('#status');
+const send = document.querySelector('#send');
+
+// '/api/...'처럼 주소 앞부분 없이 써도 이 화면을 준 서버(localhost:8080)로 요청이 감
+async function request(url, options = {}) {
+  const response = await fetch(url, options);
+  const text = await response.text();
+  let data;
+  try { data = text ? JSON.parse(text) : null; }
+  catch { throw new Error(`서버 응답을 읽을 수 없습니다. HTTP ${response.status}`); }
+  if (!response.ok) throw new Error(data?.message || `HTTP ${response.status}`);
+  return data;
+}
+function jsonOptions(method, content) {
+  // 이 헤더가 있어야 Spring이 본문을 JSON으로 읽어 @RequestBody에 넣음
+  return { method, headers: { 'Content-Type': 'application/json' },
+    body: JSON.stringify({ content }) };
+}
+async function loadQuestions() {
+  const questions = await request('/api/questions');
+  list.replaceChildren();
+  for (const question of questions) {
+    const item = document.createElement('li');
+    const text = document.createElement('p');
+    text.textContent = `#${question.id} 질문: ${question.content}\n답변: ${question.answer}`;
+    item.append(text);
+    list.append(item);
+  }
+}
+form.addEventListener('submit', async event => {
+  event.preventDefault();
+  if (!input.value.trim()) { statusBox.textContent = '질문을 입력해 주세요.'; return; }
+  send.disabled = true;
+  statusBox.textContent = '답변을 기다리고 있습니다.';
+  try {
+    await request('/api/questions', jsonOptions('POST', input.value));
+    input.value = '';
+    await loadQuestions();
+    statusBox.textContent = '등록했습니다.';
+  } catch (error) { statusBox.textContent = error.message; }
+  finally { send.disabled = false; }
+});
+loadQuestions().catch(error => { statusBox.textContent = error.message; });
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
  <p id="status" role="status" aria-live="polite"></p>
  <ul id="questions"></ul>
  <!-- static 폴더 파일은 Spring이 그대로 내려 줌. 이 파일은 http://localhost:8080/, app.js는 /app.js로 열림 -->
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

// '/api/...'처럼 주소 앞부분 없이 써도 이 화면을 준 서버(localhost:8080)로 요청이 감
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
  // 이 헤더가 있어야 Spring이 본문을 JSON으로 읽어 @RequestBody에 넣음
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
```

## 실행 확인

질문을 입력해 등록하고, 목록과 Network의 POST·GET 요청을 확인합니다. 빈 입력이 거절되는지도 확인합니다.

외부 AI·음성 결과는 실제 서버 연결 후 별도로 확인합니다.
