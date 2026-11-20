# 검증 기록

- 브랜치: 05-tts-portfolio
- Spring Boot 4.2.0 / JDK 25 / Gradle Wrapper 9.1.0
- clean build: 통과
- 자동 검증: HTTP CRUD·빈 입력 400·삭제 204·없는 번호 404 / 생성 실패 시 기존 데이터와 ID 보존 (모델 모킹) / 가상 안내문 4개 청크 로딩
- 실제 Ollama 응답·검색 품질·Python TTS 합성·브라우저 재생: 이 PC에 모델 실행 환경이 없어 미검증. README의 준비 절차 후 실제 확인 필요.

- 추가 검증: 빈 검색 시 모델 생성 호출 생략 통과

- 2026-11-19 버전 변경 검증: Temurin 25.0.5에서 Gradle Wrapper 9.1.0의 clean build 통과. 자동 테스트 4 개, 실패 0 개, 오류 0 개. Spring Boot 4.1.2 / AI 단계 Spring AI 2.0.2.

- 2026-11-20 버전 변경 검증: Temurin 25.0.5에서 Gradle Wrapper 9.1.0의 clean build 통과. 자동 테스트 4 개, 실패 0 개, 오류 0 개. Spring Boot 4.2.0 / AI 단계 Spring AI 2.1.0.
