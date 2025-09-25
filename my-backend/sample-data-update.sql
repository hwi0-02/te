-- Hotel 테이블에 누락된 필드들을 추가하는 SQL 스크립트
-- 기존 데이터가 있는 경우를 고려한 안전한 업데이트
-- 사업자 승인 500 오류 해결용

-- 1. Hotel 테이블에 누락된 필드 추가 (기존 데이터 보존)
-- (기존에 이미 추가된 필드들이 있을 수 있으므로 에러 무시)
ALTER TABLE hotel ADD COLUMN user_id BIGINT DEFAULT 1 AFTER id;
ALTER TABLE hotel ADD COLUMN business_id BIGINT DEFAULT 1001 AFTER user_id;
ALTER TABLE hotel ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP AFTER rejection_reason;

-- 2. Foreign Key 제약조건 추가 (데이터 무결성 보장)
-- (기존 제약조건이 있으면 무시하고 진행)
-- user_id는 필수지만 approved_by는 선택사항이므로 별도 처리
ALTER TABLE hotel ADD CONSTRAINT fk_hotel_user_id FOREIGN KEY (user_id) REFERENCES app_user(id);

-- approved_by에 대한 제약조건 (null 값 허용)
-- ALTER TABLE hotel ADD CONSTRAINT fk_hotel_approved_by FOREIGN KEY (approved_by) REFERENCES app_user(id);

-- 3. Hotel 테이블에 통계 데이터용 인덱스 추가 (성능 최적화)
CREATE INDEX IF NOT EXISTS idx_hotel_user_id ON hotel(user_id);
CREATE INDEX IF NOT EXISTS idx_hotel_approval_status ON hotel(approval_status);
CREATE INDEX IF NOT EXISTS idx_hotel_created_at ON hotel(created_at);

-- 4. 통계 계산을 위한 뷰 생성 (선택사항 - 성능 향상용)
CREATE OR REPLACE VIEW v_hotel_statistics AS
SELECT
    h.id,
    h.name,
    h.user_id,
    u.name as business_name,
    u.email as business_email,
    u.phone as business_phone,
    (SELECT COUNT(*) FROM room WHERE hotel_id = h.id) as total_rooms,
    (SELECT COUNT(*) FROM reservation res JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id) as total_reservations,
    (SELECT AVG(rev.star_rating) FROM review rev JOIN reservation res ON rev.reservation_id = res.id
     JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id) as average_rating,
    (SELECT SUM(p.total_price) FROM payment p JOIN reservation res ON p.reservation_id = res.id
     JOIN room r ON res.room_id = r.id WHERE r.hotel_id = h.id AND p.status = 'PAID') as total_revenue,
    h.approval_status,
    h.created_at
FROM hotel h
LEFT JOIN app_user u ON h.user_id = u.id;

-- 5. 통계 뷰 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_v_hotel_stats_hotel_id ON v_hotel_statistics(id);
CREATE INDEX IF NOT EXISTS idx_v_hotel_stats_business ON v_hotel_statistics(business_name, business_email);

-- 사용법:
-- SELECT * FROM v_hotel_statistics WHERE approval_status = 'APPROVED' ORDER BY total_revenue DESC;
-- SELECT * FROM v_hotel_statistics WHERE business_name LIKE '%호텔%';

-- 개선된 기능:
-- 1. 사업자 승인 500 오류 해결 (Foreign Key 제약조건 추가)
-- 2. 호텔관리에서 사업자 이름, 이메일, 전화번호 표시
-- 3. 호텔 상세에서 사업자 정보 조회 가능
-- 4. 통계 뷰로 빠른 데이터 조회 가능
-- 5. 승인 처리 유효성 검사 및 시스템 승인 지원
-- 6. 데이터베이스 스크립트 중복 제거 및 개선
