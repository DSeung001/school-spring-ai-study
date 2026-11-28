# 5일차-2: 익명 댓글 삭제

교안: `5일차 포트폴리오화.md` → **2. 과제: 익명 댓글** → 추가 과제.

## 변경 목적

5일차-1의 댓글마다 삭제 버튼을 붙이는 완성 HTML·JS입니다.

## 적용 준비

5일차-1의 두 API에 더해 `DELETE /api/comments/{id}` 구현. 06-result의 백엔드를 사용해도 됩니다.

적용 방법은 [퍼블리싱 안내](../README.md#적용-방법)를 따릅니다. 두 파일을 함께 복사하세요.

## 이전 단계와 비교

### `index.html`

이전 단계 `5일차-1`과 같습니다.

### `app.js`

```diff
--- 5일차-1/app.js
+++ 5일차-2/app.js
@@ -86,6 +86,17 @@
       const row = document.createElement('li');
       // createdAt 예: 2026-11-30T10:15:00 → 2026-11-30 10:15
       row.textContent = `익명 · ${comment.createdAt.replace('T', ' ').slice(0, 16)}\n${comment.content}`;
+      const removeComment = document.createElement('button');
+      removeComment.textContent = '댓글 삭제';
+      removeComment.addEventListener('click', async () => {
+        if (!confirm('이 댓글을 삭제할까요?')) return;
+        try {
+          await request(`/api/comments/${comment.id}`, { method: 'DELETE' });
+          await loadComments();
+          statusBox.textContent = '댓글을 삭제했습니다.';
+        } catch (error) { statusBox.textContent = error.message; }
+      });
+      row.append(removeComment);
       comments.append(row);
     }
   }
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
    .comments li { border: 0; padding: 4px 0; }
    .comment-form input { width: 60%; padding: 8px; }
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
  <!-- static 폴더 파일은 Spring이 그대로 내려 줌. 이 파일은 http://localhost:8081/, app.js는 /app.js로 열림 -->
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
    item.append(commentBox(question.id));
    list.append(item);
  }
}
// 질문마다 익명 댓글 목록과 입력칸을 만듦. 댓글은 질문 번호가 들어간 주소로 따로 요청함.
function commentBox(questionId) {
  const box = document.createElement('section');
  box.setAttribute('aria-label', `#${questionId} 질문의 댓글`);
  const comments = document.createElement('ul');
  comments.className = 'comments';
  const commentForm = document.createElement('form');
  commentForm.className = 'comment-form';
  const commentInput = document.createElement('input');
  commentInput.maxLength = 100;
  commentInput.placeholder = '익명 댓글 (1~100자)';
  const add = document.createElement('button');
  add.type = 'submit';
  add.textContent = '댓글 등록';
  commentForm.append(commentInput, add);
  box.append(comments, commentForm);

  async function loadComments() {
    const found = await request(`/api/questions/${questionId}/comments`);
    comments.replaceChildren();
    for (const comment of found) {
      const row = document.createElement('li');
      // createdAt 예: 2026-11-30T10:15:00 → 2026-11-30 10:15
      row.textContent = `익명 · ${comment.createdAt.replace('T', ' ').slice(0, 16)}\n${comment.content}`;
      const removeComment = document.createElement('button');
      removeComment.textContent = '댓글 삭제';
      removeComment.addEventListener('click', async () => {
        if (!confirm('이 댓글을 삭제할까요?')) return;
        try {
          await request(`/api/comments/${comment.id}`, { method: 'DELETE' });
          await loadComments();
          statusBox.textContent = '댓글을 삭제했습니다.';
        } catch (error) { statusBox.textContent = error.message; }
      });
      row.append(removeComment);
      comments.append(row);
    }
  }
  commentForm.addEventListener('submit', async event => {
    event.preventDefault();
    if (!commentInput.value.trim()) { statusBox.textContent = '댓글을 입력해 주세요.'; return; }
    add.disabled = true;
    try {
      await request(`/api/questions/${questionId}/comments`, jsonOptions('POST', commentInput.value));
      commentInput.value = '';
      await loadComments();
      statusBox.textContent = '댓글을 등록했습니다.';
    } catch (error) { statusBox.textContent = error.message; }
    finally { add.disabled = false; }
  });
  loadComments().catch(error => { comments.textContent = `댓글을 불러오지 못했습니다. ${error.message}`; });
  return box;
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

1. 댓글 삭제를 누르고 확인하면 `DELETE /api/comments/1`이 204로 응답하고 목록에서 사라집니다.
2. 다른 탭에서 이미 지운 댓글을 다시 지우면 `댓글을 찾을 수 없습니다.`가 나옵니다.
3. 댓글이 달린 질문을 삭제한 뒤 새로고침하면 질문과 댓글이 함께 사라집니다(FK `ON DELETE CASCADE`).
