# 관리자 프론트엔드

Next.js 16.0.6 / React 19.2.0 / JavaScript App Router 기반 화면입니다.
백엔드는 저장소 루트에, 프론트는 `frontend/`에 배치합니다.

## 실행
```bash
cd frontend
npm ci
npm run dev
```
접속: http://127.0.0.1:3000
빌드: `npm run build`

## 현재 범위
- 예시 데이터 기반 UI이며 로그인과 업무 API는 아직 연결하지 않았습니다.
- 변경 상태는 새로고침 시 초기화됩니다. 예시 작업 이력은 실제 작업과 연결되지 않습니다.
- 공통 메뉴가 가리키는 페이지는 각 기능 PR을 통합하면 제공됩니다.
- `lib/*-mocks.js`는 기능 화면과 대시보드가 공유하는 예시 데이터입니다.
- 사용자 사이트 주소: `NEXT_PUBLIC_USER_SITE_URL` (기본 http://localhost:5173).
- 패키지는 재현용으로 고정된 취약 버전입니다. 실데이터/공용 DB 자격증명 없이 격리 환경에서 사용합니다.

## 통합 순서
공통 기반 PR을 먼저 dev에 통합한 다음 기능 PR들을 검토합니다.
기능 브랜치는 동일한 공통 기반 커밋을 공유하므로 공통 PR 병합 시 merge commit 방식이 적합합니다.
API가 미준비 상태이므로 Swagger/Postman 검증은 미실시이며 빌드 검사와 구분합니다.
