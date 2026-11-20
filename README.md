# school-spring-ai-study

1일차 시작: Spring Boot 기본 프로젝트. 아직 Question·Controller·화면이 없습니다.

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
git switch 01-main
# java는 바이트코드 실행 명령입니다. 실행 환경이 25 계열인지 확인합니다.
java -version
# javac는 소스 컴파일 명령입니다. 컴파일러도 25 계열인지 확인합니다.
javac -version
.\gradlew.bat clean build
.\gradlew.bat bootRun
```

## 준비와 실행 확인

- Spring Boot 4.2.0 / JDK 25 / Gradle Wrapper 9.1.0을 사용합니다. AI 단계는 Spring AI 2.1.0 기준입니다.

- JDK 25, IntelliJ IDEA, 인터넷이 필요합니다. Project SDK·Gradle JVM·JAVA_HOME을 25로 맞추세요.
- Gradle Wrapper 9.1.0을 포함합니다. 최초 실행 시 Gradle과 의존성을 내려받습니다.
- 기존 서버는 Ctrl+C로 종료하고 실행하세요. Spring 포트는 8080입니다.
- `Started RagApplication` 로그를 확인하세요. 아직 `/`의 404는 정상입니다.
- 질문 내역은 메모리에만 저장되므로 서버 재시작 시 사라집니다.

## 검증 범위

빌드와 자동 검증 결과는 VALIDATION.md에 기록합니다. 외부 모델의 실제 응답 품질·합성 품질은 학교 PC에서 별도로 확인해야 합니다.

## 학생 기록

제공 코드와 본인 변경점을 구분하고, 입력 → 처리 → 결과, 실행한 질문·실제 응답·오류·미확인 항목을 날짜별로 덧붙이세요.
