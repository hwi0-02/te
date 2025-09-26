-- 편의시설 테이블
CREATE TABLE `amenity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `icon_url` varchar(255) DEFAULT NULL,
  `fee_type` enum('FREE','PAID','HOURLY') NOT NULL DEFAULT 'FREE',
  `fee_amount` int(11) DEFAULT NULL,
  `fee_unit` varchar(255) DEFAULT NULL,
  `operating_hours` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `category` enum('IN_ROOM','IN_HOTEL','LEISURE','FNB','BUSINESS','OTHER') NOT NULL DEFAULT 'OTHER',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 테이블
CREATE TABLE `app_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `date_of_birth` date NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `role` enum('USER','ADMIN','BUSINESS') NOT NULL DEFAULT 'USER',
  `is_active` bit(1) NOT NULL,
  `profile_image_url` varchar(255) DEFAULT NULL,
  `provider` enum('GOOGLE','KAKAO','LOCAL','NAVER') NOT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `social_providers` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_email` (`email`),
  UNIQUE KEY `uq_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 쿠폰 테이블
CREATE TABLE `coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `discount_type` enum('PERCENTAGE','FIXED_AMOUNT') NOT NULL,
  `discount_value` int(11) NOT NULL,
  `min_spend` int(11) NOT NULL DEFAULT 0,
  `valid_from` datetime NOT NULL,
  `valid_to` datetime DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_coupon_code` (`code`),
  KEY `idx_coupon_user` (`user_id`),
  CONSTRAINT `FK_User_TO_Coupon_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 호텔 테이블
CREATE TABLE `hotel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `business_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` varchar(255) NOT NULL,
  `star_rating` int(11) NOT NULL,
  `description` longtext DEFAULT NULL,
  `country` varchar(50) NOT NULL,
  `approval_date` datetime(6) DEFAULT NULL,
  `approval_status` enum('APPROVED','PENDING','REJECTED','SUSPENDED') DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `rejection_reason` tinytext DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hotel_user` (`user_id`),
  CONSTRAINT `FK_User_TO_Hotel_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 호텔-편의시설 연결 테이블
CREATE TABLE `hotel_amenity` (
  `hotel_id` bigint(20) NOT NULL,
  `amenity_id` bigint(20) NOT NULL,
  PRIMARY KEY (`hotel_id`,`amenity_id`),
  KEY `idx_ha_amenity` (`amenity_id`),
  CONSTRAINT `FK_Amenity_TO_Hotel_Amenity_1` FOREIGN KEY (`amenity_id`) REFERENCES `amenity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_Hotel_TO_Hotel_Amenity_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 호텔 이미지 테이블
CREATE TABLE `hotel_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint(20) NOT NULL,
  `url` tinytext NOT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  `is_cover` tinyint(1) NOT NULL DEFAULT 0,
  `caption` varchar(255) DEFAULT NULL,
  `alt_text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_hotel_sort` (`hotel_id`,`sort_no`),
  KEY `idx_himg_hotel` (`hotel_id`),
  CONSTRAINT `fk_himg_hotel` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 문의 테이블
CREATE TABLE `inquiry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  `reply` text DEFAULT NULL,
  `status` enum('ANSWERED','CLOSED','PENDING') NOT NULL,
  `subject` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 공지사항 테이블
CREATE TABLE `notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `is_active` bit(1) NOT NULL,
  `content` text DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `is_pinned` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- 객실 테이블
CREATE TABLE `room` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `room_size` varchar(50) NOT NULL,
  `capacity_min` int(11) NOT NULL,
  `capacity_max` int(11) NOT NULL,
  `check_in_time` time NOT NULL,
  `check_out_time` time NOT NULL,
  `aircon` bit(1) DEFAULT NULL,
  `bath` int(11) DEFAULT NULL,
  `bed` varchar(50) DEFAULT NULL,
  `cancel_policy` varchar(100) DEFAULT NULL,
  `free_water` bit(1) DEFAULT NULL,
  `has_window` bit(1) DEFAULT NULL,
  `original_price` int(11) DEFAULT NULL,
  `payment` varchar(50) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `shared_bath` bit(1) DEFAULT NULL,
  `smoke` bit(1) DEFAULT NULL,
  `view_name` varchar(50) DEFAULT NULL,
  `wifi` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room_hotel` (`hotel_id`),
  CONSTRAINT `FK_Hotel_TO_Room_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 예약 테이블
CREATE TABLE `reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `room_id` bigint(20) NOT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `num_adult` int(11) NOT NULL DEFAULT 0,
  `num_kid` int(11) NOT NULL DEFAULT 0,
  `start_date` timestamp NOT NULL,
  `end_date` timestamp NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('PENDING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `expires_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_res_user` (`user_id`),
  KEY `idx_res_room` (`room_id`),
  CONSTRAINT `FK_Room_TO_Reservation_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
  CONSTRAINT `FK_User_TO_Reservation_1` FOREIGN KEY (`user_id`) REFERENCES `app_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 결제 테이블
CREATE TABLE `payment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reservation_id` bigint(20) NOT NULL,
  `payment_method` varchar(255) NOT NULL,
  `base_price` int(11) NOT NULL,
  `total_price` int(11) NOT NULL,
  `tax` int(11) NOT NULL DEFAULT 0,
  `discount` int(11) NOT NULL DEFAULT 0,
  `status` enum('PAID','CANCELLED','REFUNDED') NOT NULL DEFAULT 'PAID',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `refunded_at` timestamp NULL DEFAULT NULL,
  `receipt_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_receipt_url` (`receipt_url`),
  KEY `idx_pay_res` (`reservation_id`),
  CONSTRAINT `FK_Reservation_TO_Payment_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 리뷰 테이블
CREATE TABLE `review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reservation_id` bigint(20) NOT NULL,
  `wrote_on` timestamp NOT NULL DEFAULT current_timestamp(),
  `star_rating` int(11) NOT NULL DEFAULT 5,
  `content` longtext DEFAULT NULL,
  `image` longtext DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_review_reservation` (`reservation_id`),
  CONSTRAINT `FK_Reservation_TO_Review_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 객실 이미지 테이블
CREATE TABLE `room_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `url` tinytext NOT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  `is_cover` tinyint(1) NOT NULL DEFAULT 0,
  `caption` varchar(255) DEFAULT NULL,
  `alt_text` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_sort` (`room_id`,`sort_no`),
  KEY `idx_rimg_room` (`room_id`),
  CONSTRAINT `fk_rimg_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 객실 재고 테이블
CREATE TABLE `room_inventory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `total_quantity` int(11) NOT NULL,
  `available_quantity` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_room_day` (`room_id`,`date`),
  KEY `idx_inv_room` (`room_id`),
  CONSTRAINT `FK_Room_TO_Room_Inventory_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 객실 가격 정책 테이블
CREATE TABLE `room_price_policy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `room_id` bigint(20) NOT NULL,
  `season_type` enum('PEAK','OFF_PEAK','HOLIDAY') NOT NULL DEFAULT 'OFF_PEAK',
  `day_type` enum('WEEKDAY','FRI','SAT','SUN') NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `price` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rpp_room` (`room_id`),
  CONSTRAINT `FK_Room_TO_Room_Price_Policy_1` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;