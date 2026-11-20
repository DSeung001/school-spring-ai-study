# 검증 기록

- 브랜치: 06-result
- Spring Boot 4.2.0 / JDK 25 / Gradle Wrapper 9.9.2
- clean build: 통과
- 자동 검증: HTTP CRUD·빈 입력 400·삭제 204·없는 번호 404 / 생성 실패 시 기존 데이터와 ID 보존 (모델 모킹) / 가상 안내문 4개 청크 로딩
- 실제 Ollama 응답·검색 품질·Python TTS 합성·브라우저 재생: 이 PC에 모델 실행 환경이 없어 미검증. README의 준비 절차 후 실제 확인 필요.

- 추가 검증: 빈 검색 시 모델 생성 호출 생략 통과 / 임시 HTTP 서버로 TTS 텍스트 전달·바이트 반환·상위 서버 오류의 502 변환 통과

- 이전 환경에서 통과(이번 버전 변경에서는 미재검증): 실제 Spring HTTP 실행·4개 청크 초기 인덱싱·정적 화면 제공·입력 검증·CRUD·RAG sources·TTS 프록시. Ollama/TTS는 임시 HTTP 스텁으로 검증했으며 실제 모델 품질 검증이 아님.

- 2026-11-19 버전 변경 검증: Temurin 25.0.5에서 Gradle Wrapper 9.1.0의 clean build 통과. 자동 테스트 6 개, 실패 0 개, 오류 0 개. Spring Boot 4.1.2 / AI 단계 Spring AI 2.0.2.

- 2026-11-20 버전 변경 검증: Temurin 25.0.5에서 Gradle Wrapper 9.1.0의 clean build 통과. 자동 테스트 6 개, 실패 0 개, 오류 0 개. Spring Boot 4.2.0 / AI 단계 Spring AI 2.1.0.

- 2026-11-20 Initializr 기준 정리 검증: Temurin 25.0.5에서 Gradle Wrapper 9.9.2의 clean build 통과. 자동 테스트 6 개, 실패 0 개, 오류 0 개. application.yaml·RagApplicationTests·validation-test 의존성 반영.
