## 관리자 패널 DB 데이터 로딩 계획

### 1) 현황 진단 및 목표
- 증상: `/admin` 진입은 가능하나 목록/상세 데이터가 비어 있음 또는 로드 실패
- 가설: (1) DB 연결/권한/charset 설정 문제 (2) API 레이어에서 쿼리/ORM 매핑 오류 (3) 관리자 전용 엔드포인트 미구현 또는 권한가드(ADMIN)만 있고 실제 데이터 조회 누락 (4) 페이징/필터 파라미터 검증 실패 (5) CORS/쿠키/세션 미전달로 백엔드에서 유저 컨텍스트 해석 실패
- 목표: 관리자 주요 화면(대시보드/호텔/객실/예약/결제/문의/공지/리뷰/쿠폰)의 리스트·상세 데이터를 DB에서 정상 조회 및 페이징/검색/정렬 동작

---

### 2) 인프라/환경 점검 (우선순위 A)
1. DB 접속 환경 변수 정합성 확인: `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `DB_TIMEZONE=+09:00`, `DB_CHARSET=utf8mb4`
2. 연결 테스트: 서버 컨테이너/호스트에서 DB로 TCP 접속 확인 및 간단 `SELECT 1` 헬스체크 구현/확인
3. 커넥션 풀 설정: 최소/최대 커넥션, idle timeout, acquire timeout 합리화 (관리자 화면 동시 조회 대비)
4. 초기 세팅: 연결 직후 `SET NAMES utf8mb4;`, `SET time_zone = '+09:00';` 적용 옵션 확인

---

### 3) DB 스키마·인덱스 확인 (우선순위 B)
- 스키마: `app_user`, `Hotel`, `hotel_image`, `Amenity`, `Hotel_Amenity`, `Room`, `room_image`, `Room_Inventory`, `Room_Price_Policy`, `Reservation`, `Payment`, `Coupon`, `Review`, `notice`, `inquiry` (db.md 참조)
- 현 인덱스: 주요 FK 및 일부 유니크/키 존재
- 추가 제안 인덱스(목록 성능 개선):
  - `Hotel(approval_status)`, `Hotel(name)`, `Hotel(address)`
  - `Reservation(status, created_at)` 복합 또는 단일 인덱스
  - `Payment(created_at)`
  - `inquiry(status, created_at)`, `notice(created_at, is_pinned)`
- 샘플 데이터: db.md의 INSERT 시나리오 실행 또는 마이그레이션 시드 스크립트화

---

### 4) 백엔드 API 정비 (우선순위 S)
공통 원칙
- 관리자 권한 가드: `role=ADMIN` 필수, 모든 `/api/admin/**`에 적용
- 입력 검증: `page(>=1)`, `size(<=100)`, `sort`, `order`, 검색어 `q` 등
- 보안: 파라미터 바인딩(Prepared Statement/ORM 바인딩), N+1 방지(JOIN/배치 로딩), 에러 표준화(JSON: code, message)

엔드포인트 (예시)
- GET `/api/admin/dashboard`
  - 반환: 카운트/지표
  - 쿼리 스케치:
    - 호텔 수: `SELECT COUNT(*) FROM Hotel;`
    - 객실 수: `SELECT COUNT(*) FROM Room;`
    - 예약 상태별 수: `SELECT status, COUNT(*) FROM Reservation GROUP BY status;`
    - 최근 7일 결제 합: `SELECT SUM(total_price) FROM Payment WHERE created_at >= NOW() - INTERVAL 7 DAY;`
    - 미답변 문의 수: `SELECT COUNT(*) FROM inquiry WHERE status='PENDING';`

- GET `/api/admin/hotels?status=&q=&page=&size=&sort=&order=`
  - 필터: `approval_status`, 검색: `name/address` LIKE
  - 페이징: `LIMIT ?, ?` 또는 ORM 페이징, 총건수 별도 쿼리
  - 대표 이미지: `LEFT JOIN hotel_image hi ON hi.hotel_id=h.id AND hi.is_cover=TRUE`

- GET `/api/admin/rooms?hotelId=&page=&size=`
  - 필터: `hotel_id`
  - 이미지: `room_image` 커버 조인

- GET `/api/admin/reservations?status=&start=&end=&page=&size=`
  - 조인: `Reservation r JOIN Room rm ON r.room_id=rm.id JOIN Hotel h ON rm.hotel_id=h.id`

- GET `/api/admin/payments?start=&end=&page=&size=`
  - 필터: 기간, 정렬: `created_at DESC`

- GET `/api/admin/inquiries?status=&page=&size=`
- GET `/api/admin/notices?page=&size=`
- GET `/api/admin/reviews?page=&size=`
- GET `/api/admin/coupons?userId=&page=&size=`

서비스/리포지토리 레이어
- "조회 전용" 메서드 일괄 정리: `findHotels`, `findRoomsByHotel`, `listReservations`, `listPayments`, `listInquiries`, `listNotices`, `listReviews`, `listCoupons`
- 공통 페이징 DTO: `Page<T> { items, page, size, total }`

예시 쿼리 스케치 (ORM/SQL 공용)
```sql
-- 호텔 목록 (검색 + 상태)
SELECT h.id, h.name, h.address, h.star_rating, h.approval_status,
       (SELECT url FROM hotel_image WHERE hotel_id=h.id AND is_cover=TRUE ORDER BY sort_no LIMIT 1) AS cover_url
FROM Hotel h
WHERE (@status IS NULL OR h.approval_status=@status)
  AND (@q IS NULL OR (h.name LIKE CONCAT('%',@q,'%') OR h.address LIKE CONCAT('%',@q,'%')))
ORDER BY h.id DESC
LIMIT @offset, @limit;
```

---

### 5) 관리자 UI 데이터 연동 (우선순위 A)
- 데이터 훅/클라이언트: 공통 fetch 유틸(인증 포함), 캐시(SWR/React Query 유사 패턴) 선택
- 화면 단위
  - 대시보드: 지표 카드·스파크라인, 로딩 스켈레톤, 에러/빈 상태
  - 호텔 관리: 테이블(이름/주소/등급/상태/대표 이미지), 검색/상태 필터, 페이지네이션
  - 객실 관리: 호텔 필터, 썸네일, 체크인/아웃 정보
  - 예약/결제: 기간 필터, 상태 필터, 합계 표시
  - 문의/공지/리뷰/쿠폰: 기본 테이블 + 상태/기간 필터
- 공통 UX: 로딩 표시, 빈 데이터 안내, 재시도, 컬럼 정렬

---

### 6) 에러 처리·관측성 (우선순위 A)
- 백엔드: 표준 에러 응답 스키마(`{code,message,details}`), 구조화 로깅, 슬로우쿼리 로그 활성화
- 프런트: 에러 토스트/스낵바, 재시도 버튼, 401/403 처리(로그인/권한 안내)
- 모니터링: API P95 지연, 에러율 대시보드, 주요 목록 쿼리 실행계획 정기 점검

---

### 7) 테스트·검증 (우선순위 S)
1. 로컬 DB에 db.md 스키마/시드 적용 후 관리자 로그인
2. 각 엔드포인트 cURL/Postman 스모크 테스트
   - 예: `GET /api/admin/hotels?page=1&size=20&status=APPROVED&q=강남`
3. E2E: 관리자 화면에서 목록이 노출되고 페이징/검색/필터가 동작하는지 확인
4. 경계 테스트: 빈 결과(0건), 1건, 대량(>1만건)에서 응답 시간 측정

---

### 8) 완료 기준 (Acceptance Criteria)
- 관리자 각 화면에서 DB 실데이터가 표시되고, 페이지네이션/검색/정렬이 작동한다
- 비로그인/권한없는 요청은 401/403으로 차단된다
- P95 API 응답시간 200ms 이하(페이지 당 20~50건 기준, 캐시 미사용 상태)
- 슬로우쿼리 미발생(>1s 없음), 에러 로그 0건(스모크·E2E 기준)

---

### 9) 작업 체크리스트
- [ ] DB 연결/타임존/charset 설정 재확인 및 헬스체크 엔드포인트 통과
- [ ] 관리자 전용 목록/상세 조회 엔드포인트 구현 및 권한가드 적용
- [ ] 공통 페이징 DTO/유효성 검증 도입
- [ ] 핵심 목록(호텔/예약/결제/문의/공지) 쿼리 최적화 및 인덱스 점검/추가
- [ ] UI 연동(스켈레톤/빈/에러/페이지네이션/필터/정렬)
- [ ] 스모크/E2E 테스트 통과 및 성능 기준 충족

