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
