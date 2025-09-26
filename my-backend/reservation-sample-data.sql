-- =====================================================
-- 예약 관련 완전한 임시 데이터 INSERT 문
-- =====================================================
-- 이 스크립트는 reservation, payment, review 관련 완전한 테스트 데이터를 생성합니다.
-- 기존 app_user, hotel, room 데이터가 있다고 가정합니다.

-- 1. 사용자 테스트 데이터 생성
INSERT INTO app_user (name, phone, email, password, date_of_birth, address, created_on, role, is_active, profile_image_url, provider, provider_id, social_providers) VALUES
('김민수', '01012345678', 'minsu.kim@example.com', '$2a$10$example.hash.password', '1990-05-15', '서울특별시 강남구 역삼동', NOW(), 'USER', b'1', NULL, 'LOCAL', NULL, '{}'),
('이영희', '01023456789', 'younghee.lee@example.com', '$2a$10$example.hash.password', '1985-08-22', '서울특별시 서초구 서초동', NOW(), 'USER', b'1', NULL, 'LOCAL', NULL, '{}'),
('박철수', '01034567890', 'chulsoo.park@example.com', '$2a$10$example.hash.password', '1992-03-10', '부산광역시 해운대구', NOW(), 'USER', b'1', NULL, 'LOCAL', NULL, '{}'),
('최지현', '01045678901', 'jihyun.choi@example.com', '$2a$10$example.hash.password', '1988-12-05', '대구광역시 수성구', NOW(), 'USER', b'1', NULL, 'LOCAL', NULL, '{}'),
('홍길동', '01056789012', 'gildong.hong@example.com', '$2a$10$example.hash.password', '1995-07-18', '광주광역시 서구', NOW(), 'USER', b'1', NULL, 'LOCAL', NULL, '{}');

-- 2. 사업자 사용자 데이터 생성
INSERT INTO app_user (name, phone, email, password, date_of_birth, address, created_on, role, is_active, profile_image_url, provider, provider_id, social_providers) VALUES
('호텔왕', '01098765432', 'hotelking@business.com', '$2a$10$example.hash.password', '1975-03-20', '서울특별시 중구 명동', NOW(), 'BUSINESS', b'1', NULL, 'LOCAL', NULL, '{}'),
('리조트대표', '01087654321', 'resort@business.com', '$2a$10$example.hash.password', '1980-09-15', '제주특별자치도 제주시', NOW(), 'BUSINESS', b'1', NULL, 'LOCAL', NULL, '{}');

-- 3. 호텔 데이터 생성
INSERT INTO hotel (user_id, business_id, name, address, star_rating, description, country, approval_status, created_at) VALUES
((SELECT id FROM app_user WHERE email = 'hotelking@business.com'), 1234567890, '그랜드 서울 호텔', '서울특별시 중구 명동길 123', 5, '서울 중심가에 위치한 5성급 럭셔리 호텔입니다. 최고의 서비스와 편의시설을 제공합니다.', '한국', 'APPROVED', NOW()),
((SELECT id FROM app_user WHERE email = 'resort@business.com'), 9876543210, '제주 오션뷰 리조트', '제주특별자치도 제주시 애월읍 해안로 456', 4, '제주 바다를 한눈에 내려다보는 최고의 리조트입니다. 가족 단위 여행객에게 인기가 많습니다.', '한국', 'APPROVED', NOW());

-- 4. 룸 데이터 생성
INSERT INTO room (hotel_id, name, room_size, capacity_min, capacity_max, check_in_time, check_out_time, aircon, bath, bed, cancel_policy, free_water, has_window, original_price, payment, price, shared_bath, smoke, view_name, wifi) VALUES
-- 그랜드 서울 호텔의 룸들
(1, '디럭스 더블룸', '더블', 1, 2, '15:00:00', '11:00:00', 1, 1, '더블베드', '체크인 1일 전까지 무료 취소', 1, 1, 200000, 'CARD', 180000, 0, 0, '시티뷰', 1),
(1, '이그제큐티브 스위트', '스위트', 2, 4, '15:00:00', '11:00:00', 1, 2, '킹베드 + 소파베드', '체크인 2일 전까지 무료 취소', 1, 1, 500000, 'CARD', 450000, 0, 0, '한강뷰', 1),
(1, '스탠다드 트윈룸', '트윈', 1, 2, '15:00:00', '11:00:00', 1, 1, '트윈베드', '체크인 1일 전까지 무료 취소', 1, 1, 150000, 'CARD', 130000, 0, 0, '시티뷰', 1),

-- 제주 오션뷰 리조트의 룸들
(2, '오션뷰 디럭스', '더블', 2, 3, '16:00:00', '11:00:00', 1, 1, '더블베드', '체크인 3일 전까지 무료 취소', 1, 1, 300000, 'CARD', 280000, 0, 0, '오션뷰', 1),
(2, '패밀리 룸', '패밀리', 2, 6, '16:00:00', '11:00:00', 1, 2, '킹베드 + 번크베드', '체크인 3일 전까지 무료 취소', 1, 1, 400000, 'CARD', 380000, 0, 0, '오션뷰', 1),
(2, '프리미엄 스위트', '스위트', 2, 4, '16:00:00', '11:00:00', 1, 2, '킹베드 + 거실', '체크인 5일 전까지 무료 취소', 1, 1, 700000, 'CARD', 650000, 0, 0, '프라이빗 테라스', 1);

-- 5. 예약 데이터 생성 (다양한 상태와 기간)
INSERT INTO reservation (user_id, room_id, transaction_id, num_adult, num_kid, start_date, end_date, created_at, status, expires_at) VALUES
-- 완료된 예약들
((SELECT id FROM app_user WHERE email = 'minsu.kim@example.com'), 1, 'TXN-2024-0001', 2, 0, '2024-01-15 15:00:00', '2024-01-17 11:00:00', '2024-01-10 10:30:00', 'COMPLETED', NULL),
((SELECT id FROM app_user WHERE email = 'younghee.lee@example.com'), 4, 'TXN-2024-0002', 2, 1, '2024-02-20 16:00:00', '2024-02-23 11:00:00', '2024-02-15 14:20:00', 'COMPLETED', NULL),
((SELECT id FROM app_user WHERE email = 'chulsoo.park@example.com'), 2, 'TXN-2024-0003', 3, 1, '2024-03-10 15:00:00', '2024-03-12 11:00:00', '2024-03-05 09:15:00', 'COMPLETED', NULL),

-- 진행중인 예약들
((SELECT id FROM app_user WHERE email = 'jihyun.choi@example.com'), 5, 'TXN-2024-0004', 4, 2, '2024-12-25 16:00:00', '2024-12-27 11:00:00', '2024-12-20 16:45:00', 'PENDING', '2024-12-24 23:59:59'),
((SELECT id FROM app_user WHERE email = 'gildong.hong@example.com'), 3, 'TXN-2024-0005', 1, 0, '2024-12-30 15:00:00', '2025-01-02 11:00:00', '2024-12-27 11:30:00', 'PENDING', '2024-12-29 23:59:59'),

-- 취소된 예약들
((SELECT id FROM app_user WHERE email = 'minsu.kim@example.com'), 6, 'TXN-2024-0006', 2, 0, '2024-04-05 16:00:00', '2024-04-08 11:00:00', '2024-03-28 08:20:00', 'CANCELLED', NULL),
((SELECT id FROM app_user WHERE email = 'younghee.lee@example.com'), 1, 'TXN-2024-0007', 1, 0, '2024-05-12 15:00:00', '2024-05-14 11:00:00', '2024-05-01 13:10:00', 'CANCELLED', NULL),

-- 최근 예약들 (다양한 날짜)
((SELECT id FROM app_user WHERE email = 'chulsoo.park@example.com'), 4, 'TXN-2024-0008', 3, 0, '2025-01-15 16:00:00', '2025-01-18 11:00:00', '2024-12-28 10:00:00', 'PENDING', '2025-01-14 23:59:59'),
((SELECT id FROM app_user WHERE email = 'jihyun.choi@example.com'), 2, 'TXN-2024-0009', 2, 0, '2025-02-10 15:00:00', '2025-02-12 11:00:00', '2024-12-28 15:30:00', 'PENDING', '2025-02-09 23:59:59'),
((SELECT id FROM app_user WHERE email = 'gildong.hong@example.com'), 5, 'TXN-2024-0010', 4, 1, '2025-03-20 16:00:00', '2025-03-24 11:00:00', '2024-12-28 20:15:00', 'PENDING', '2025-03-19 23:59:59');

-- 6. 결제 데이터 생성
INSERT INTO payment (reservation_id, payment_method, base_price, total_price, tax, discount, status, created_at, refunded_at, receipt_url) VALUES
-- 완료된 예약의 결제들
(1, 'CREDIT_CARD', 360000, 390000, 30000, 0, 'PAID', '2024-01-10 10:35:00', NULL, 'https://receipt.example.com/TXN-2024-0001'),
(2, 'KAKAO_PAY', 840000, 882000, 42000, 0, 'PAID', '2024-02-15 14:25:00', NULL, 'https://receipt.example.com/TXN-2024-0002'),
(3, 'NAVER_PAY', 900000, 945000, 45000, 0, 'PAID', '2024-03-05 09:20:00', NULL, 'https://receipt.example.com/TXN-2024-0003'),

-- 진행중인 예약의 결제들
(4, 'CREDIT_CARD', 1520000, 1596000, 76000, 0, 'PAID', '2024-12-20 16:50:00', NULL, 'https://receipt.example.com/TXN-2024-0004'),
(5, 'TOSS_PAY', 390000, 409500, 19500, 0, 'PAID', '2024-12-27 11:35:00', NULL, 'https://receipt.example.com/TXN-2024-0005'),

-- 취소된 예약의 결제들 (환불 처리)
(6, 'CREDIT_CARD', 1950000, 2047500, 97500, 0, 'REFUNDED', '2024-03-28 08:25:00', '2024-04-02 15:30:00', 'https://receipt.example.com/TXN-2024-0006'),
(7, 'BANK_TRANSFER', 180000, 189000, 9000, 0, 'REFUNDED', '2024-05-01 13:15:00', '2024-05-10 09:45:00', 'https://receipt.example.com/TXN-2024-0007'),

-- 최근 예약의 결제들
(8, 'CREDIT_CARD', 840000, 882000, 42000, 0, 'PAID', '2024-12-28 10:05:00', NULL, 'https://receipt.example.com/TXN-2024-0008'),
(9, 'KAKAO_PAY', 900000, 945000, 45000, 0, 'PAID', '2024-12-28 15:35:00', NULL, 'https://receipt.example.com/TXN-2024-0009'),
(10, 'PAYPAL', 1520000, 1596000, 76000, 0, 'PAID', '2024-12-28 20:20:00', NULL, 'https://receipt.example.com/TXN-2024-0010');

-- 7. 리뷰 데이터 생성 (완료된 예약에 대해서만)
INSERT INTO review (reservation_id, wrote_on, star_rating, content, image) VALUES
(1, '2024-01-18 14:30:00', 5, '정말 만족스러운 숙박이었습니다! 직원분들이 친절하고 시설도 깨끗했어요. 한강뷰가 정말 아름다웠습니다. 다음에도 꼭 재방문하겠습니다.', 'https://images.example.com/review-001.jpg'),
(2, '2024-02-24 10:15:00', 4, '제주 바다 전망이 환상적이었어요! 아이들이 정말 좋아했습니다. 다만 조식 메뉴가 조금 아쉬웠어요. 그래도 전반적으로 만족합니다.', 'https://images.example.com/review-002.jpg'),
(3, '2024-03-13 16:45:00', 5, '스위트룸이 정말 넓고 고급스러웠어요. 특히 욕실이 너무 좋았습니다. 비즈니스 출장으로 갔는데 최고의 선택이었어요!', NULL);

-- 8. 룸 이미지 데이터 생성
INSERT INTO room_image (room_id, url, sort_no, is_cover, caption, alt_text) VALUES
-- 그랜드 서울 호텔 룸 이미지들
(1, 'https://images.example.com/hotel1-room1-cover.jpg', 0, 1, '디럭스 더블룸 전경', '그랜드 서울 호텔 디럭스 더블룸'),
(1, 'https://images.example.com/hotel1-room1-bathroom.jpg', 1, 0, '디럭스 더블룸 욕실', '그랜드 서울 호텔 디럭스 더블룸 욕실'),
(2, 'https://images.example.com/hotel1-room2-cover.jpg', 0, 1, '이그제큐티브 스위트 전경', '그랜드 서울 호텔 이그제큐티브 스위트'),
(2, 'https://images.example.com/hotel1-room2-livingroom.jpg', 1, 0, '이그제큐티브 스위트 거실', '그랜드 서울 호텔 이그제큐티브 스위트 거실'),
(3, 'https://images.example.com/hotel1-room3-cover.jpg', 0, 1, '스탠다드 트윈룸 전경', '그랜드 서울 호텔 스탠다드 트윈룸'),

-- 제주 오션뷰 리조트 룸 이미지들
(4, 'https://images.example.com/hotel2-room1-cover.jpg', 0, 1, '오션뷰 디럭스 전경', '제주 오션뷰 리조트 오션뷰 디럭스'),
(4, 'https://images.example.com/hotel2-room1-oceanview.jpg', 1, 0, '오션뷰 디럭스 바다 전망', '제주 오션뷰 리조트 오션뷰 디럭스 바다 전망'),
(5, 'https://images.example.com/hotel2-room2-cover.jpg', 0, 1, '패밀리 룸 전경', '제주 오션뷰 리조트 패밀리 룸'),
(5, 'https://images.example.com/hotel2-room2-bunkbed.jpg', 1, 0, '패밀리 룸 이층침대', '제주 오션뷰 리조트 패밀리 룸 이층침대'),
(6, 'https://images.example.com/hotel2-room3-cover.jpg', 0, 1, '프리미엄 스위트 전경', '제주 오션뷰 리조트 프리미엄 스위트'),
(6, 'https://images.example.com/hotel2-room3-terrace.jpg', 1, 0, '프리미엄 스위트 프라이빗 테라스', '제주 오션뷰 리조트 프리미엄 스위트 테라스');

-- 9. 호텔 이미지 데이터 생성
INSERT INTO hotel_image (hotel_id, url, sort_no, is_cover, caption, alt_text) VALUES
-- 그랜드 서울 호텔 이미지들
(1, 'https://images.example.com/hotel1-exterior-cover.jpg', 0, 1, '그랜드 서울 호텔 외관', '그랜드 서울 호텔 정면 외관'),
(1, 'https://images.example.com/hotel1-lobby.jpg', 1, 0, '그랜드 서울 호텔 로비', '그랜드 서울 호텔 럭셔리 로비'),
(1, 'https://images.example.com/hotel1-restaurant.jpg', 2, 0, '그랜드 서울 호텔 레스토랑', '그랜드 서울 호텔 파인다이닝 레스토랑'),
(1, 'https://images.example.com/hotel1-pool.jpg', 3, 0, '그랜드 서울 호텔 수영장', '그랜드 서울 호텔 실내 수영장'),

-- 제주 오션뷰 리조트 이미지들
(2, 'https://images.example.com/hotel2-exterior-cover.jpg', 0, 1, '제주 오션뷰 리조트 외관', '제주 오션뷰 리조트 바다 전망 외관'),
(2, 'https://images.example.com/hotel2-beach.jpg', 1, 0, '제주 오션뷰 리조트 프라이빗 해변', '제주 오션뷰 리조트 전용 해변'),
(2, 'https://images.example.com/hotel2-infinity-pool.jpg', 2, 0, '제주 오션뷰 리조트 인피니티 풀', '제주 오션뷰 리조트 인피니티 풀'),
(2, 'https://images.example.com/hotel2-sunset.jpg', 3, 0, '제주 오션뷰 리조트 일몰', '제주 오션뷰 리조트에서 바라본 아름다운 일몰');

-- =====================================================
-- 데이터 확인 쿼리들
-- =====================================================

-- 예약 현황 확인
-- SELECT r.id, u.name, h.name as hotel_name, rm.name as room_name, r.transaction_id, r.status, r.start_date, r.end_date 
-- FROM reservation r 
-- JOIN app_user u ON r.user_id = u.id 
-- JOIN room rm ON r.room_id = rm.id 
-- JOIN hotel h ON rm.hotel_id = h.id 
-- ORDER BY r.created_at DESC;

-- 결제 현황 확인
-- SELECT p.id, r.transaction_id, p.payment_method, p.total_price, p.status 
-- FROM payment p 
-- JOIN reservation r ON p.reservation_id = r.id 
-- ORDER BY p.created_at DESC;

-- 리뷰 현황 확인
-- SELECT rv.id, u.name, h.name as hotel_name, rv.star_rating, rv.content 
-- FROM review rv 
-- JOIN reservation r ON rv.reservation_id = r.id 
-- JOIN app_user u ON r.user_id = u.id 
-- JOIN room rm ON r.room_id = rm.id 
-- JOIN hotel h ON rm.hotel_id = h.id 
-- ORDER BY rv.wrote_on DESC;
