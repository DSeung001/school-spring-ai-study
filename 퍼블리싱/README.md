# 퍼블리싱 단계별 코드

1~5일차 교안에서 HTML·JavaScript가 바뀌는 단계만 수업 순서대로 분리했습니다. 모든 브랜치에 같은 자료를 제공합니다. 각 폴더에는 해당 시점의 완성 `index.html`, `app.js`와 전후 비교 문서가 있습니다.

| 순서 | 교안 | 추가되는 기능 | 완성 백엔드를 제공하는 브랜치 |
|---|---|---|---|
| [2일차-1](2일차-1/README.md) | 2일차 4절 | 질문 등록·목록 조회 | 02-spring-mvc-crud |
| [2일차-2](2일차-2/README.md) | 2일차 5절 | 질문 수정·삭제 | 02-spring-mvc-crud |
| [4일차-1](4일차-1/README.md) | 4일차 4절 | 검색 근거 표시 | 05-tts-portfolio |
| [5일차-1](5일차-1/README.md) | 5일차 3절 | 답변 듣기·오디오 재생 | 06-result |

1·3일차에는 새로운 HTML·JS 코드가 없습니다. 3일차 AI 연결은 기존 2일차 화면을 사용합니다. 콘솔 디버깅 예제와 자유 수정 과제는 별도 단계로 만들지 않았습니다.

DB 준비는 [DB 연결 안내](../DB-연결.md)를 따릅니다.

## 적용 방법

`01-main`은 기본 프로젝트, `02-spring-mvc-crud`는 DB 기반 MVC CRUD 완성본입니다. `03`~`05`는 해당 일차의 수업 시작 상태이며 `06-result`는 최종본입니다. 폴더가 있어도 아직 해당 API가 구현되지 않았을 수 있으므로 단계 문서의 백엔드 준비 조건을 먼저 확인하세요. 아래 예시는 2일차 등록·조회·수정·삭제 API 구현을 마친 뒤 적용합니다.

PowerShell:

```powershell
New-Item -ItemType Directory -Force src/main/resources/static | Out-Null
Copy-Item 퍼블리싱/2일차-2/index.html src/main/resources/static/index.html -Force
Copy-Item 퍼블리싱/2일차-2/app.js src/main/resources/static/app.js -Force
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
mkdir -p src/main/resources/static
cp 퍼블리싱/2일차-2/index.html src/main/resources/static/index.html
cp 퍼블리싱/2일차-2/app.js src/main/resources/static/app.js
./gradlew bootRun
```

원하는 단계로 폴더 이름을 바꾸고 두 파일을 함께 복사합니다. 위 명령은 기존 정적 파일을 덮어씁니다. 본인 수정 내용이 있다면 먼저 커밋하세요.

브라우저에서 [http://localhost:8081/](http://localhost:8081/)을 엽니다. `server.port`를 변경했다면 해당 포트를 사용합니다. HTML을 직접 열거나 별도 정적 서버에서 실행하지 않고 Spring을 통해 접속합니다. `/app.js`와 `/api/...`는 화면을 제공한 Spring 서버로 요청됩니다.

## 전후 비교 읽기

각 단계 README의 diff는 바로 이전 퍼블리싱 단계와 비교합니다. `-`는 이전 코드, `+`는 이후 코드이며 첫 단계는 신규 파일 추가입니다. 이어지는 전체 코드와 같은 폴더의 HTML·JS 파일은 내용이 같습니다.

원본 교안은 personal-assistant 저장소의 `70 강의/2026 서울디지텍고 스프링 특강/`에 있습니다. 설명과 코드의 기본 Spring 포트는 교안과 동일하게 localhost:8081입니다. 실제 API 요청은 상대 경로를 유지합니다.

## 검증 기록 · 2026-11-23

- 4개 단계 모두 JavaScript 구문, HTML 요소와 선택자 연결, 문서 전체 코드와 파일 일치, diff 적용 후 코드 일치를 확인했습니다.
- 모의 DOM·서버 응답으로 등록·조회·수정·삭제, 취소·빈 입력·200자 제한, API 오류·잘못된 JSON 처리를 확인했습니다. RAG 근거 유무, TTS 성공·실패·1000자 제한, 자동 재생 거절·오디오 오류·오디오 URL 해제도 확인했습니다.
- MySQL에 연결한 `02-spring-mvc-crud`의 임시 복사본에서 실제 Spring을 실행하고 `2일차-2` 정적 파일 제공, POST·GET·PUT·DELETE, 삭제 204 응답과 빈 입력 400 응답을 확인했습니다. 검증 서버만 포트 18081을 사용했습니다.
- 실제 Ollama의 RAG 검색·생성 결과, TTS 음성 합성·브라우저 청취는 이번 검증에 포함하지 않았습니다. 해당 서버를 준비한 뒤 각 단계의 실행 확인을 따릅니다.
