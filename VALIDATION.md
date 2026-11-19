# 검증 기록

- 브랜치: rag
- JDK 21 / Gradle Wrapper 8.14.3
- clean build: 통과
- 자동 검증: HTTP CRUD·빈 입력 400·삭제 204·없는 번호 404 / 생성 실패 시 기존 데이터와 ID 보존 (모델 모킹)
- 실제 Ollama 응답·검색 품질·Python TTS 합성·브라우저 재생: 이 PC에 모델 실행 환경이 없어 미검증. README의 준비 절차 후 실제 확인 필요.
