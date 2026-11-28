# MySQL·MyBatis 연결

## 브랜치와 실행 준비

| 브랜치 | 제공 상태 | MySQL 필요 |
|---|---|---|
| 01-main | 1일차 기본 프로젝트 | 아니요 |
| 02-spring-mvc-crud | 2일차 시작: 메모리 등록·조회·입력 검증·ApiErrorHandler | 아니요 |
| 03-spring-ai | 3일차 시작: DB 기반 MVC CRUD 완성본 | 예 |
| 04-rag | 4일차 시작: DB 저장과 Ollama 답변 완성본 | 예 |
| 05-tts-portfolio | 5일차 시작: DB 저장과 RAG·검색 근거 완성본. 익명 댓글 과제 시작 | 예 |
| 06-result | 최종본: RAG와 5일차 익명 댓글(등록·조회·삭제)까지 완성 | 예 |

1일차에서 ApiErrorHandler까지 완성하며 `02`를 받으면 그 상태로 시작합니다. POST·GET만 있고 DB·화면·수정·삭제는 없습니다. 2일차는 MySQL 설치부터 시작합니다. DB 기반 CRUD 복구용 완성본은 `03`입니다. `03`~`05`의 이름은 그날 배울 개념입니다.

## 1. MySQL 8.4 LTS 설치

공식 [다운로드](https://dev.mysql.com/downloads/mysql/8.4.html)에서 8.4 LTS와 본인 OS·CPU를 선택합니다. [공식 설치 안내](https://dev.mysql.com/doc/refman/8.4/en/installing.html)를 함께 확인하세요. MySQL Server와 SQL 접속 도구는 역할이 다릅니다. Workbench 설치는 필수가 아닙니다.

### Windows · PowerShell

1. Windows x64용 MSI를 실행합니다. 설치 후 **MySQL Configurator**로 서버를 구성합니다. 8.4는 예전 8.0용 MySQL Installer를 사용하지 않습니다.
2. TCP 포트 `3306`, Windows 서비스 등록과 시작을 확인하고 관리자 `root` 비밀번호를 직접 정합니다. 학교 PC의 관리자 권한과 설치 허용 여부는 강사가 미리 확인합니다.
3. 아래처럼 클라이언트를 실행합니다. 설치 경로가 다르면 실제 경로로 바꿉니다. 비밀번호는 `-p` 뒤에 적지 않고 프롬프트에서 입력합니다.

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" --default-character-set=utf8mb4 -u root -p
```

### macOS · Terminal(zsh)

1. 공식 DMG·PKG에서 Apple Silicon은 ARM64, Intel은 x86_64를 선택합니다. 다운로드 화면의 지원 macOS 버전도 확인합니다.
2. 패키지 설치와 초기 설정에서 `root` 비밀번호를 정하고 MySQL 서버를 시작합니다. 중지 상태면 시스템 설정의 MySQL 패널에서 시작합니다.
3. 기본 설치 경로의 클라이언트로 접속합니다.

```bash
/usr/local/mysql/bin/mysql --default-character-set=utf8mb4 -u root -p
```

두 OS 모두 `mysql>` 프롬프트에서 다음을 실행해 서버 접속을 확인합니다.

```sql
SELECT VERSION();
```

## 2. DB 만들기와 root 계정 사용

설치할 때 정한 root 계정으로 접속해 DB를 만듭니다. 강의에서는 별도 사용자를 만들지 않고 root로 연결합니다. application.yaml의 password에는 설치 시 정한 root 비밀번호를 넣습니다. 실제 비밀번호를 Git에 올리지 않습니다.

```sql
CREATE DATABASE IF NOT EXISTS school_ai CHARACTER SET utf8mb4;
EXIT;
```

root 비밀번호는 MySQL 설치 시 설정한 값을 사용합니다.

## 3. Spring 접속 설정과 실행

`application.yaml`의 기존 `spring.datasource`를 아래처럼 수정합니다. password의 자리표시자를 설치할 때 정한 root 비밀번호로 바꿉니다. 비밀번호는 작은따옴표로 감싸고, 비밀번호 자체에 작은따옴표가 있다면 두 번 적습니다. 예: `ab'cd` → `'ab''cd'`.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/school_ai?connectionTimeZone=Asia/Seoul
    username: root
    password: '본인의_root_비밀번호'
```

터미널에서 DB 비밀번호를 입력하거나 환경변수를 설정할 필요 없이 IntelliJ의 실행 버튼으로 시작합니다. `spring:`을 중복해서 추가하지 말고 기존 datasource 부분을 수정합니다. 명령으로 실행할 때도 비밀번호 입력 없이 아래 명령만 사용합니다.

```powershell
.\gradlew.bat bootRun
```

```bash
./gradlew bootRun
```

실제 비밀번호를 적은 application.yaml 변경은 커밋하지 않습니다. 저장소에는 자리표시자만 둡니다. 브랜치를 바꾸기 전에는 로컬 설정을 따로 보관하고, 새 브랜치의 설정에도 비밀번호를 입력합니다.

[http://localhost:8081/](http://localhost:8081/)에서 접속합니다. Spring은 8081, MySQL은 3306입니다.

`schema.sql`은 없는 테이블만 생성합니다. 기존 질문을 지우거나 예제 데이터를 넣지 않습니다. 질문은 MySQL에 남으므로 Spring을 종료·재실행해도 유지됩니다. 번호는 DB의 `AUTO_INCREMENT`가 생성하며 삭제·롤백으로 빈 번호가 생겨도 정상입니다.

## 4. 저장 흐름과 코드 위치

```text
com.example.rag
├── RagApplication.java
├── controller   HTTP 요청·응답
├── service      업무 처리·AI 호출·트랜잭션
├── repository   QuestionMapper, RAG 단계의 SourceMapper와 SQL
├── model        Question, RAG 단계의 Source, 5일차의 Comment
├── dto          QuestionRequest, 이후 GeneratedAnswer, 5일차의 CommentRequest
├── config       RAG 단계의 설정
├── exception    ApiErrorHandler
└── practice     ObjectPractice
```

빈 폴더는 만들지 않습니다. Model은 DB 자체가 아니라 Java 데이터이며, 정적 `index.html`·`app.js`가 View입니다. Mapper는 MyBatis가 구현하는 DB 접근 인터페이스입니다. 애너테이션에 SQL을 한 번 작성하고 `#{...}`로 값을 전달합니다. 생성된 질문 번호는 `@Options(useGeneratedKeys = true)`로 돌려받습니다.

`questions`는 질문·답변을 저장합니다. RAG 단계의 `question_sources`는 당시 근거의 순서·제목·청크 번호·본문·점수를 저장하며 질문 삭제 시 함께 삭제됩니다. 안내문 임베딩 검색은 기존 `SimpleVectorStore`를 사용합니다. 관계형 DB의 질문 내역과 벡터 검색 저장소는 역할이 다릅니다.

AI 호출은 쓰기 트랜잭션 전에 끝내고, 질문·답변과 근거는 `TransactionTemplate`으로 함께 저장합니다. AI 생성 실패 시 저장을 시작하지 않고, DB 저장 도중 실패하면 전체를 롤백합니다. SQL·접속 정보는 오류 응답에 노출하지 않으며 DB 작업 실패는 503과 `message` JSON으로 안내합니다.

## 5. 테스트는 전용 DB에서 실행

root 계정으로 테스트 DB를 생성합니다.

```sql
CREATE DATABASE IF NOT EXISTS school_ai_test CHARACTER SET utf8mb4;
```

테스트할 때는 application.yaml의 datasource.url을 `jdbc:mysql://localhost:3306/school_ai_test?connectionTimeZone=Asia/Seoul`로 바꾸고 root 비밀번호를 입력한 뒤 `./gradlew test` 또는 `.\gradlew.bat test`를 실행합니다. 테스트가 끝나면 url을 school_ai로 되돌립니다. 통합 테스트는 이름이 `_test`로 끝나는 DB에서만 질문을 삭제합니다. 01·02 브랜치는 DB 설정 없이 테스트합니다.

RAG 테스트는 시작 시 실제 임베딩 생성을 끄고, AI 응답 테스트 대역과 실제 MySQL을 사용합니다. 일반 실행은 안내문 인덱싱을 수행하므로 Ollama와 모델을 준비해야 합니다.

## 막혔을 때

- `Connection refused`: MySQL 서비스 실행과 3306 포트 확인.
- `Access denied`: 사용자·비밀번호·허용 호스트·권한 확인. 설치 시 정한 root 비밀번호와 application.yaml의 username 설정을 확인.
- `Unknown database`: DB 생성과 application.yaml의 url 확인.
- 비밀번호 오류: application.yaml의 password와 MySQL 설치 시 정한 root 비밀번호 확인.
- 서버 재시작 후 목록이 다름: application.yaml의 url이 같은지 확인. 테스트 DB와 수업 DB를 구분.

## 버전 기준

MySQL 8.4 LTS, MyBatis Spring Boot Starter 4.1.0, MySQL Connector/J는 Spring Boot의 의존성 관리 버전을 사용합니다. JDK 25·Spring Boot 4.2.0·Gradle 9.9.2는 기존 수업 기준을 유지합니다.
