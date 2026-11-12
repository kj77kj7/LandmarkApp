# Landmark

걸은 만큼 세계 랜드마크(해운대 → 오사카성 → 에펠탑 …)를 여행하는 걷기 앱입니다.
걸음 수 동기화, 랜드마크 해금, 업적, 칭호, 친구, 알림, 주간·월간·구별·친구 랭킹을 제공합니다.

| 폴더 | 내용 | 기술 |
|---|---|---|
| 루트 (`src/`) | 백엔드 API 서버 | Spring Boot 3.5 · Java 22 · MySQL 8 · Spring Security + JWT · Gmail SMTP |
| `frontend/` | 모바일 앱 (원본: min05ji26/landmark_front) | Expo 54 · React Native |

## 백엔드 실행 방법

1. **MySQL에 데이터베이스 만들기**

   ```sql
   CREATE DATABASE landmark DEFAULT CHARACTER SET utf8mb4;
   ```

2. **비밀 설정 파일 만들기**

   `application-local.yml.example`을 같은 폴더에 `application-local.yml`로 복사하고 값을 채웁니다.
   이 파일은 `.gitignore`에 들어 있어 깃에 올라가지 않습니다.

   | 항목 | 설명 |
   |---|---|
   | `spring.mail.username` / `password` | Gmail 주소와 **앱 비밀번호** (Google 계정 → 보안 → 앱 비밀번호) |
   | `spring.datasource.username` / `password` | MySQL 계정 |
   | `jwt.secret` | JWT 서명 키, 영문 32자 이상 아무 문자열 |

   파일 대신 환경변수 `MAIL_USERNAME`, `MAIL_PASSWORD`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`로 넣어도 됩니다.

3. **서버 실행**

   ```bash
   ./gradlew bootRun        # Windows: gradlew.bat bootRun
   ```

   처음 실행하면 랜드마크(`data.sql`), 업적, 테스트 계정(`TestDataRunner`)이 자동으로 들어갑니다.
   테이블은 `ddl-auto: update`라 재시작해도 데이터가 유지됩니다.
   배포할 때는 `--spring.profiles.active=prod`로 실행하면 테스트 계정을 만들지 않습니다.

## 테스트

```bash
./gradlew test
```

MySQL 없이 메모리 DB(H2)로 돌아갑니다. 테스트 설정은 `src/test/resources/application.yml`에 있습니다.

## 앱(frontend) 실행 방법

```bash
cd frontend
npm install
npx expo start          # 휴대폰 Expo Go로 QR 스캔, 또는 w 키로 웹 실행
```

서버 주소는 `frontend/src/constants/constants.js`의 `API_URL`에서 정합니다.
웹은 `http://localhost:8080`, 휴대폰은 Expo 개발 서버를 띄운 PC의 IP(같은 와이파이)를 씁니다.
백엔드 CORS는 `localhost:3000/5173/8080/8081`과 `192.168.219.*`를 허용합니다 (`config/SecurityConfig.java`).

## 백엔드 폴더 구조

```
src/main/java/project/landmark
├── config/       SecurityConfig(인증·CORS), TestDataRunner(초기 데이터)
├── controller/   REST API
├── dto/          요청·응답 객체
├── entity/       JPA 엔티티
├── exception/    전역 예외 처리 (오류를 ApiResponse JSON으로)
├── repository/   Spring Data JPA
├── security/     JWT 필터·발급, 이메일 인증번호 저장소
└── service/      비즈니스 로직
```

## 인증

로그인(`POST /api/auth/login`)하면 JWT가 `data`로 옵니다. 이후 요청에는 헤더를 붙입니다.

```
Authorization: Bearer <토큰>
```

로그인 없이 쓸 수 있는 API: 회원가입·로그인·중복 확인·아이디 찾기·비밀번호 재설정(`/api/auth/**`, `/api/email-auth/**`),
랜드마크 목록(`GET /api/landmarks`), 주간·월간·구별 랭킹, 전체 업적 목록(`GET /api/achievements`, `GET /api/achievements/{id}`).
나머지는 모두 토큰이 필요하며, 토큰이 없거나 만료되면 **401**과 함께 `{"success": false, "message": "로그인이 필요합니다."}`가 옵니다.

## API 변경 사항 (2026-11 보안 수정)

`frontend/`의 비밀번호 재설정 화면(`src/screens/AuthScreen.js`)은 이미 아래 변경에 맞춰 인증번호 입력칸을 추가했습니다.
다른 클라이언트가 있다면 아래 표대로 맞춰야 합니다.

| API | 변경 내용 |
|---|---|
| `POST /api/auth/check-reset-info` | 요청은 그대로 `{username, email}`. 이제 확인에 성공하면 **그 이메일로 6자리 인증번호를 보냅니다.** |
| `POST /api/auth/reset-password` | 요청에 **`code`(인증번호)가 필수**: `{username, code, newPassword}`. 인증번호는 5분간 유효하고 5번 틀리면 다시 받아야 합니다. 새 비밀번호는 8자 이상. |
| `GET /api/auth/find-id` | **삭제.** 아이디 찾기는 `POST /api/email-auth/send` → `POST /api/email-auth/verify`로만 합니다. |
| `GET /api/ranking/friends/{username}` | 로그인 필요, **본인 username만** 조회 가능 (다르면 403). |
| `GET /api/achievements/user/{username}` | 로그인 필요. |
| `POST /api/achievements`, `POST /api/achievements/unlock` | 삭제 (사용하지 않던 API). |
| `DELETE /api/notifications/{id}`, `POST …/{id}/accept`, `POST …/{id}/reject` | 내게 온 알림만 처리 가능 (다르면 403). |
| `GET /api/notifications` | 응답 항목에서 `user` 객체가 빠졌습니다. 나머지 필드(`id`, `type`, `message`, `senderName`, `senderProfileImage`, `read`, `createdAt`)는 같습니다. |
| 회원가입 | 이미 있는 닉네임이면 거절됩니다 (`check-nickname`과 같은 기준). |
| 토큰 없이 로그인 필요한 API 호출 | 500 대신 **401**. |
