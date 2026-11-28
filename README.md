# school-spring-ai-study

학교 안내 도우미를 MVC·MySQL·Spring AI·RAG 순서로 만들고, 5일차 과제로 익명 댓글을 추가하는 수업 코드입니다.

- [DB 설치·연결과 브랜치 기준](DB-연결.md)
- [단계별 HTML·JS와 전후 비교](퍼블리싱/README.md)
- [검증 기록](검증.md)

`01-main`은 DB 없는 기본 프로젝트, `02-spring-mvc-crud`는 메모리 등록·조회·입력 검증·ApiErrorHandler 완성본입니다. `03-spring-ai`는 AI 연결 전, `04-rag`는 RAG 연결 전, `05-tts-portfolio`는 5일차 익명 댓글 과제 시작 상태이며(브랜치 이름의 tts는 이전 계획의 이름이고 TTS는 다루지 않음) `06-result`는 최종본입니다.

03~06 실행 전 DB 안내에 따라 MySQL을 준비하고 application.yaml에 root 비밀번호를 입력하세요. Spring 서버 포트는 **8081**입니다. 04 이후는 Ollama, 05 이후는 임베딩 모델도 필요합니다. Gemini는 선택 강사 시연입니다.

코드를 변경했다면 커밋한 뒤 브랜치를 전환하세요. 02의 질문은 메모리에 저장되어 재시작하면 사라집니다. 03~06의 질문·답변은 MySQL에 저장되며 서버 재시작 후에도 유지됩니다. RAG 단계는 검색 근거도 함께 저장하고, 06은 익명 댓글도 저장합니다.

## 4일차 추가 과제와 최종 답안

`05-tts-portfolio`와 `06-result`에는 4일차 추가 과제 5개의 최종 답안이 들어 있습니다. `04-rag`는 본 수업 시작 상태 그대로입니다. 문제와 해설·diff는 4일차 교안의 「추가 과제」와 「최종 답안」에 있습니다.

| 난이도 | 과제 | 최종 동작 | 주요 코드 |
|---|---|---|---|
| 하 | 질문 단건 조회 | `GET /api/questions/{id}`, 없는 번호는 404 | QuestionController, QuestionService |
| 하 | 검색 개수 설정 | `app.rag.top-k`, 없으면 3, 1 미만이면 시작 실패 | NoticeSearchService, application.yaml |
| 중 | 질문·답변 키워드 검색 | `GET /api/questions?keyword=도서관` | QuestionService, QuestionMapper |
| 중 | 검색 전용 API | `POST /api/search`, 답변 생성·DB 저장 없음 | SearchController, NoticeSearchService |
| 상 | LEFT JOIN 조회 | 목록·검색·단건을 SELECT 한 번으로 조회 | QuestionMapper, QuestionSourceRow, QuestionService |

API 사용 예시입니다. 서버 포트는 8081입니다.

```bash
curl http://localhost:8081/api/questions/1
curl -G http://localhost:8081/api/questions --data-urlencode "keyword=도서관"
curl -X POST http://localhost:8081/api/search -H "Content-Type: application/json" \
  -d '{"content":"책은 몇 권 빌려요?"}'
```

```powershell
[Console]::OutputEncoding = [Text.Encoding]::UTF8
Invoke-RestMethod http://localhost:8081/api/questions/1 | ConvertTo-Json -Depth 5
Invoke-RestMethod "http://localhost:8081/api/questions?keyword=$([uri]::EscapeDataString('도서관'))" | ConvertTo-Json -Depth 5
'{"content":"책은 몇 권 빌려요?"}' | Out-File -Encoding utf8 body.json
curl.exe -i http://localhost:8081/api/search -H "Content-Type: application/json" --data-binary "@body.json"
```

키워드는 앞뒤 공백을 지우고 질문·답변에서만 찾습니다. 비어 있으면 전체 목록, 결과가 없으면 `[]`입니다. 검색 전용 API는 질문 등록과 같은 입력 검증(공백 불가·200자 이내)을 사용하고, 임베딩 모델이 없으면 503을 반환합니다.


## 5일차 익명 댓글 과제 답안

`06-result`에는 5일차 과제인 익명 댓글의 최종 답안이 들어 있습니다. 시작 상태는 `05-tts-portfolio`이며, 화면은 [퍼블리싱 5일차-1·5일차-2](퍼블리싱/README.md)로 제공합니다. 문제와 해설은 5일차 교안의 「과제: 익명 댓글」과 「같이 풀기」에 있습니다.

| 구분 | 요청 | 응답 | 주요 코드 |
|---|---|---|---|
| 필수 | `POST /api/questions/{questionId}/comments` | 201 댓글. 빈 값·100자 초과 400, 없는 질문 404 | CommentController, CommentService, CommentMapper |
| 필수 | `GET /api/questions/{questionId}/comments` | 200 댓글 배열(번호순). 없는 질문 404 | CommentMapper |
| 추가 | `DELETE /api/comments/{id}` | 204. 없는 번호 404 | CommentService |
| 추가 | 질문 삭제 | 댓글도 함께 삭제 | schema.sql의 `ON DELETE CASCADE` |

```bash
curl -X POST http://localhost:8081/api/questions/1/comments -H "Content-Type: application/json" \
  -d '{"content":"저도 궁금해요"}'
curl http://localhost:8081/api/questions/1/comments
curl -X DELETE http://localhost:8081/api/comments/1
```
