# school-spring-ai-study

2일차 시작: 객체와 샘플 JSON 응답까지 완성했습니다.

## 브랜치와 복구 기준

| 브랜치 | 상태 |
|---|---|
| `01-main` | 1일차 시작: Spring Boot 기본 프로젝트. 아직 Question·Controller·화면이 없습니다. |
| `02-spring-mvc-crud` | 2일차 시작: 객체와 샘플 JSON 응답까지 완성했습니다. |
| `03-spring-ai` | 3일차 시작: 질문 CRUD·입력 검증·정적 화면까지 완성했습니다. |
| `04-rag` | 4일차 시작: Ollama AI 답변과 생성 실패 시 데이터 보존까지 완성했습니다. |
| `05-tts-portfolio` | 5일차 시작: 안내문 검색·RAG·검색 근거 표시까지 완성했습니다. |
| `06-result` | 5일차 최종본: RAG 답변 듣기(TTS)까지 완성했습니다. |

브랜치 이름은 그날 배울 개념이며 코드는 수업 시작 상태입니다. 06-result는 최종본입니다.
수업 코드를 저장하거나 커밋한 다음 아래처럼 전환하세요. 작업 파일을 지우는 명령은 사용하지 않습니다.

```powershell
git switch 02-spring-mvc-crud
java -version
javac -version
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

## 준비와 실행 확인

- JDK 21, IntelliJ IDEA, 인터넷이 필요합니다. Project SDK·Gradle JVM·JAVA_HOME을 21로 맞추세요.
- Gradle Wrapper 8.14.3을 포함합니다. 최초 실행 시 Gradle과 의존성을 내려받습니다.
- 기존 서버는 Ctrl+C로 종료하고 실행하세요. Spring 포트는 8080입니다.
- http://localhost:8080/api/questions/sample 에서 id·content·answer JSON을 확인하세요. 아직 `/` 화면은 없습니다.
- 질문 내역은 메모리에만 저장되므로 서버 재시작 시 사라집니다.

## 검증 범위

빌드와 자동 검증 결과는 VALIDATION.md에 기록합니다. 외부 모델의 실제 응답 품질·합성 품질은 학교 PC에서 별도로 확인해야 합니다.

## 학생 기록

제공 코드와 본인 변경점을 구분하고, 입력 → 처리 → 결과, 실행한 질문·실제 응답·오류·미확인 항목을 날짜별로 덧붙이세요.
