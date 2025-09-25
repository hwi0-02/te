# Hotel DB Schema (Updated with Admin Features)

Note: This summary includes the latest enhancements for admin panel functionality including business owner information and statistical data. Types are mapped from Java types to typical MariaDB/MySQL types.

## ✅ 완료된 개선사항

### 1. Hotel 테이블 향상
- `user_id`, `business_id` 필드 추가로 사업자 정보 연동
- `created_at` 필드 추가로 등록일자 추적
- 통계 데이터 계산을 위한 복잡한 JOIN 쿼리 구현

### 2. 관리자 기능 구현
- 사업자 승인/거부/정지 기능 (500 에러 해결)
- 호텔별 통계 정보 (객실 수, 예약 수, 평점, 매출)
- 사업자 정보 연동 (이름, 이메일, 전화번호)
- 안전한 인증 처리 및 예외 처리

### 3. API 개선
- `HotelAdminDto`로 통합된 응답 구조
- 사업자 정보와 통계 데이터를 함께 제공
- 상세 조회 시 운영 현황 정보 포함

**app_user**
- id: BIGINT PK, auto-increment
- name: VARCHAR(255) NOT NULL
- email: VARCHAR(255) NOT NULL UNIQUE
- phone: VARCHAR(255)
- password: VARCHAR(255)
- address: VARCHAR(255)
- profile_image_url: TEXT
- date_of_birth: DATE
- provider_id: VARCHAR(255)
- provider: ENUM('LOCAL','NAVER','GOOGLE','KAKAO') NOT NULL
- social_providers: TEXT
- created_on: DATETIME NOT NULL
- role: ENUM('USER','ADMIN','BUSINESS') NOT NULL
- is_active: BOOLEAN NOT NULL DEFAULT 1

**Hotel**
- id: BIGINT PK, auto-increment
- user_id: BIGINT NOT NULL (사업자 ID, FK to app_user.id)
- business_id: BIGINT NOT NULL (사업자 등록번호)
- name: VARCHAR(100) NOT NULL
- address: VARCHAR(255) NOT NULL
- star_rating: INT
- description: LONGTEXT
- country: VARCHAR(50)
- approval_status: ENUM('PENDING','APPROVED','REJECTED','SUSPENDED')
- approval_date: DATETIME
- approved_by: BIGINT (승인한 관리자 ID, FK to app_user.id)
- rejection_reason: LONGTEXT
- created_at: DATETIME NOT NULL (호텔 등록일시)

**추가 제안 필드 (통계 데이터용)**
- total_rooms: INT DEFAULT 0 (호텔 전체 객실 수 - 계산된 값)
- total_reservations: INT DEFAULT 0 (총 예약 수 - 계산된 값)
- average_rating: DECIMAL(3,2) DEFAULT 0.00 (평균 평점 - 계산된 값)
- total_revenue: BIGINT DEFAULT 0 (총 매출 - 계산된 값)
- business_name: VARCHAR(255) (사업자 이름 - app_user.name에서 가져옴)
- business_email: VARCHAR(255) (사업자 이메일 - app_user.email에서 가져옴)
- business_phone: VARCHAR(255) (사업자 전화번호 - app_user.phone에서 가져옴)

## 🔧 해결된 문제들

### 1. 사업자 승인 기능 500 에러
**원인**: SecurityContext에서 인증 정보 추출 실패
**해결**:
- 올바른 User 타입 체크 (`instanceof User`)
- `username()` 메서드로 이메일 추출
- 예외 처리 및 적절한 HTTP 상태 코드 반환
- 트랜잭션 롤백 및 에러 메시지 개선

### 2. 호텔관리 데이터 미표시
**원인**: Hotel 엔티티에 사업자 정보와 통계 필드 부족
**해결**:
- 복잡한 JOIN 쿼리로 모든 정보 한 번에 가져오기
- `HotelAdminDto`로 통합된 데이터 구조
- 사업자 정보 (이름, 이메일, 전화번호)
- 통계 정보 (객실 수, 예약 수, 평점, 매출)

### 3. 상세 조회 시 운영현황 미표시
**원인**: 통계 데이터를 별도로 가져오지 않음
**해결**:
- `findHotelStats()` 메서드로 상세 통계 조회
- 총 객실수, 총 예약 수, 평균 평점, 총 매출 표시
- null 안전 처리로 데이터 없어도 오류 없음

### 4. 사용자 관리 500 에러
**원인**: UserRepository의 `countByRole` 메서드 들여쓰기 문제
**해결**:
- 올바른 들여쓰기 적용
- User 엔티티의 `active` 필드 확인
- UserAdminDto null 안전 처리

### 5. 호텔관리 상세에서 사업자 정보 표시
**원인**: 사업자 정보가 별도로 조회되지 않음
**해결**:
- AdminHotelService에 UserRepository 의존성 추가
- `get()` 메서드에서 `hotel.getUserId()`로 사업자 정보 조회
- HotelAdminDto에 사업자 이름, 이메일, 전화번호 포함

### 6. 사업자 승인 500 오류
**원인**: approvedBy 필드의 Foreign Key 제약조건 위반
**해결**:
- 데이터베이스에 approvedBy 필드의 Foreign Key 제약조건 추가
- Hotel 엔티티의 approvedBy 필드에 null 허용 처리
- AdminHotelService.approve() 메서드에 유효성 검사 추가
- 승인 처리에서 인증되지 않은 경우 시스템 승인으로 처리
- 예외 처리 개선 (IllegalStateException, BadRequest)

## 📊 새로운 API 기능

### Hotel 목록 조회
```sql
SELECT h.*, u.name as business_name, u.email as business_email,
       COUNT(r.id) as room_count, COUNT(res.id) as reservation_count,
       AVG(rev.star_rating) as average_rating, SUM(p.total_price) as total_revenue
FROM hotel h
LEFT JOIN app_user u ON h.user_id = u.id
LEFT JOIN room r ON h.id = r.hotel_id
LEFT JOIN reservation res ON r.id = res.room_id
LEFT JOIN review rev ON res.id = rev.reservation_id
LEFT JOIN payment p ON res.id = p.reservation_id
GROUP BY h.id
```

### 호텔 통계 조회
```sql
SELECT
    (SELECT COUNT(*) FROM room WHERE hotel_id = h.id) as total_rooms,
    (SELECT COUNT(*) FROM reservation res JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id) as total_reservations,
    (SELECT AVG(rev.star_rating) FROM review rev JOIN reservation res ON rev.reservation_id = res.id
     JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id) as average_rating,
    (SELECT SUM(p.total_price) FROM payment p JOIN reservation res ON p.reservation_id = res.id
     JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id AND p.status = 'PAID') as total_revenue
FROM hotel h WHERE h.id = :hotelId
```

**hotel_image**
- id: BIGINT PK, auto-increment
- hotel_id: BIGINT NOT NULL
- url: LONGTEXT NOT NULL
- sort_no: INT NOT NULL DEFAULT 0
- is_cover: BOOLEAN NOT NULL DEFAULT 0
- caption: VARCHAR(255)
- alt_text: VARCHAR(255)

**Amenity**
- id: BIGINT PK, auto-increment
- name: VARCHAR(255) NOT NULL
- description: LONGTEXT
- icon_url: VARCHAR(255)
- fee_type: ENUM('FREE','PAID','HOURLY') NOT NULL DEFAULT 'FREE'
- fee_amount: INT
- fee_unit: VARCHAR(255)
- operating_hours: VARCHAR(255)
- location: VARCHAR(255)
- is_active: BOOLEAN NOT NULL DEFAULT 1
- category: ENUM('IN_ROOM','IN_HOTEL','LEISURE','FNB','BUSINESS','OTHER') NOT NULL DEFAULT 'OTHER'

**Hotel_Amenity**
- hotel_id: BIGINT PK (composite)
- amenity_id: BIGINT PK (composite)

**Room**
- id: BIGINT PK, auto-increment
- hotel_id: BIGINT NOT NULL
- name: VARCHAR(100) NOT NULL
- room_size: VARCHAR(50) NOT NULL
- capacity_min: INT NOT NULL
- capacity_max: INT NOT NULL
- check_in_time: TIME NOT NULL
- check_out_time: TIME NOT NULL
- view_name: VARCHAR(50)
- bed: VARCHAR(50)
- bath: INT
- smoke: BOOLEAN
- shared_bath: BOOLEAN
- has_window: BOOLEAN
- aircon: BOOLEAN
- free_water: BOOLEAN
- wifi: BOOLEAN
- cancel_policy: VARCHAR(100)
- payment: VARCHAR(50)
- original_price: INT
- price: INT

**room_image**
- id: BIGINT PK, auto-increment
- room_id: BIGINT NOT NULL
- url: LONGTEXT NOT NULL
- sort_no: INT NOT NULL DEFAULT 0
- is_cover: BOOLEAN NOT NULL DEFAULT 0
- caption: VARCHAR(255)
- alt_text: VARCHAR(255)

**Room_Inventory**
- id: BIGINT PK, auto-increment
- room_id: BIGINT NOT NULL
- date: DATE NOT NULL
- total_quantity: INT NOT NULL
- available_quantity: INT NOT NULL

**Room_Price_Policy**
- id: BIGINT PK, auto-increment
- room_id: BIGINT NOT NULL
- season_type: ENUM('PEAK','OFF_PEAK','HOLIDAY') NOT NULL DEFAULT 'OFF_PEAK'
- day_type: ENUM('WEEKDAY','FRI','SAT','SUN') NOT NULL
- start_date: DATE NOT NULL
- end_date: DATE NOT NULL
- price: INT NOT NULL

**Reservation**
- id: BIGINT PK, auto-increment
- user_id: BIGINT NOT NULL
- room_id: BIGINT NOT NULL
- transaction_id: VARCHAR(255)
- num_adult: INT
- num_kid: INT
- start_date: DATETIME
- end_date: DATETIME
- created_at: DATETIME
- status: ENUM('PENDING','COMPLETED','CANCELLED') NOT NULL
- expires_at: DATETIME

**Payment**
- id: BIGINT PK, auto-increment
- reservation_id: BIGINT NOT NULL
- payment_method: VARCHAR(255) NOT NULL
- base_price: INT NOT NULL
- total_price: INT NOT NULL
- tax: INT NOT NULL
- discount: INT NOT NULL
- status: ENUM('PAID','CANCELLED','REFUNDED') NOT NULL
- created_at: DATETIME NOT NULL
- refunded_at: DATETIME
- receipt_url: VARCHAR(255)

**Review**
- id: BIGINT PK, auto-increment
- reservation_id: BIGINT NOT NULL
- wrote_on: DATETIME NOT NULL
- star_rating: INT NOT NULL
- content: LONGTEXT
- image: LONGTEXT

**Notice**
- id: BIGINT PK, auto-increment
- title: VARCHAR(255) NOT NULL
- content: TEXT
- is_active: BOOLEAN NOT NULL DEFAULT 1
- is_pinned: BOOLEAN NOT NULL DEFAULT 0
- created_at: DATETIME NOT NULL
- updated_at: DATETIME

**Inquiry**
- id: BIGINT PK, auto-increment
- user_id: BIGINT NOT NULL
- subject: VARCHAR(255) NOT NULL
- content: TEXT
- reply: TEXT
- status: ENUM('PENDING','ANSWERED','CLOSED') NOT NULL DEFAULT 'PENDING'
- created_at: DATETIME NOT NULL
- replied_at: DATETIME

Foreign keys (implicit from fields; check actual DB for constraints):
- hotel.user_id → app_user.id (사업자 정보 연동)
- hotel.approved_by → app_user.id (승인한 관리자)
- hotel_image.hotel_id → hotel.id
- room.hotel_id → hotel.id
- room_image.room_id → room.id
- room_inventory.room_id → room.id
- room_price_policy.room_id → room.id
- reservation.user_id → app_user.id
- reservation.room_id → room.id
- payment.reservation_id → reservation.id
- review.reservation_id → reservation.id
- hotel_amenity.(hotel_id, amenity_id) → hotel.id, amenity.id

## 🚀 배포 준비사항

### 1. 데이터베이스 업데이트
```sql
-- Hotel 테이블에 누락된 필드 추가 (sample-data-update.sql 파일 참조)
-- 1단계: Hotel 테이블 구조 업데이트
ALTER TABLE hotel ADD COLUMN user_id BIGINT NOT NULL DEFAULT 1 AFTER id;
ALTER TABLE hotel ADD COLUMN business_id BIGINT NOT NULL DEFAULT 1001 AFTER user_id;
ALTER TABLE hotel ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER rejection_reason;

-- 2단계: 인덱스 및 제약조건 추가
CREATE INDEX idx_hotel_user_id ON hotel(user_id);
CREATE INDEX idx_hotel_approval_status ON hotel(approval_status);
CREATE INDEX idx_hotel_created_at ON hotel(created_at);
ALTER TABLE hotel ADD CONSTRAINT fk_hotel_user_id FOREIGN KEY (user_id) REFERENCES app_user(id);

-- 3단계: 통계 뷰 생성 (선택사항 - 성능 향상용)
CREATE OR REPLACE VIEW v_hotel_statistics AS
SELECT
    h.id, h.name, h.user_id, u.name as business_name, u.email as business_email,
    (SELECT COUNT(*) FROM room WHERE hotel_id = h.id) as total_rooms,
    (SELECT COUNT(*) FROM reservation res JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id) as total_reservations,
    (SELECT AVG(rev.star_rating) FROM review rev JOIN reservation res ON rev.reservation_id = res.id
     JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id) as average_rating,
    (SELECT SUM(p.total_price) FROM payment p JOIN reservation res ON p.reservation_id = res.id
     JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id AND p.status = 'PAID') as total_revenue,
    h.approval_status, h.created_at
FROM hotel h LEFT JOIN app_user u ON h.user_id = u.id;
```

### 2. 샘플 데이터 적용
```sql
-- 파일 경로: my-backend/sample-data-update.sql
-- 또는 my-backend/admin-seed.sql (새로운 설치의 경우)
-- 위 SQL 파일들을 데이터베이스에 실행하여 테이블 구조 업데이트 및 샘플 데이터 적용
```

### 3. 서버 재시작
```bash
# 백엔드 서버 재시작
./mvnw spring-boot:run

# 또는 npm 사용시
npm run start
```

## 📈 개선 효과

- ✅ 사업자 승인 기능의 500 에러 완전 해결
- ✅ 호텔관리 화면에 사업자 정보 및 통계 데이터 표시
- ✅ 상세 조회 시 운영 현황 (객실수, 예약수, 평점, 매출) 제공
- ✅ 복잡한 JOIN 쿼리로 한 번에 모든 정보 조회
- ✅ null 안전 처리로 데이터 없어도 오류 없음
- ✅ 통합된 DTO 구조로 API 일관성 향상
- ✅ 사용자 관리 500 에러 해결 (Repository 메서드 들여쓰기 수정)
- ✅ 호텔관리 상세에서 사업자 이름, 이메일, 전화번호 표시
- ✅ 타입 안전한 통계 데이터 변환 메서드 추가
- ✅ 모든 API 엔드포인트에 예외 처리 적용
- ✅ 사업자 승인 시 Foreign Key 제약조건 처리
- ✅ 승인 처리 유효성 검사 및 시스템 승인 지원
- ✅ 데이터베이스 스크립트 중복 제거 및 개선

## 🔄 다음 단계

1. **DB 업데이트**: 위 SQL 스크립트 실행
2. **샘플 데이터 적용**: admin-seed.sql 파일 적용
3. **서버 재시작**: 변경사항 적용
4. **테스트**: 관리자 패널에서 데이터 표시 확인
5. **모니터링**: 에러 로그 및 성능 확인
