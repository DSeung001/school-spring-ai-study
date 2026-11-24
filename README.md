# school-spring-ai-study

학교 안내 도우미를 MVC·MySQL·Spring AI·RAG·TTS 순서로 만드는 수업 코드입니다.

- [DB 설치·연결과 브랜치 기준](DB-연결.md)
- [단계별 HTML·JS와 전후 비교](퍼블리싱/README.md)
- [검증 기록](검증.md)

`01-main`은 DB 없는 기본 프로젝트, `02-spring-mvc-crud`는 메모리 등록·조회·입력 검증·ApiErrorHandler 완성본입니다. `03-spring-ai`는 AI 연결 전, `04-rag`는 RAG 연결 전, `05-tts-portfolio`는 TTS 연결 전 상태이며 `06-result`는 최종본입니다.

03~06 실행 전 DB 안내에 따라 MySQL을 준비하고 application.yaml에 root 비밀번호를 입력하세요. Spring 서버 포트는 **8081**입니다. 04 이후는 Ollama, 05 이후는 임베딩 모델, 06은 TTS 서버도 필요합니다. Gemini는 선택 강사 시연입니다.

코드를 변경했다면 커밋한 뒤 브랜치를 전환하세요. 02의 질문은 메모리에 저장되어 재시작하면 사라집니다. 03~06의 질문·답변은 MySQL에 저장되며 서버 재시작 후에도 유지됩니다. RAG 단계는 검색 근거도 함께 저장합니다.
