# Hotel DB Schema (Inferred)

Note: This summary is inferred from JPA entities currently in the codebase. Actual DDL may differ slightly depending on the database and naming conventions. Types are mapped from Java types to typical MariaDB/MySQL types.

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
- name: VARCHAR(100) NOT NULL
- address: VARCHAR(255) NOT NULL
- star_rating: INT
- description: LONGTEXT
- country: VARCHAR(50)
- approval_status: ENUM('PENDING','APPROVED','REJECTED','SUSPENDED')
- approval_date: DATETIME
- approved_by: BIGINT
- rejection_reason: LONGTEXT

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
- hotel_image.hotel_id → Hotel.id
- room.hotle_id → Hotel.id
- room_image.room_id → Room.id
- room_inventory.room_id → Room.id
- room_price_policy.room_id → Room.id
- reservation.user_id → app_user.id
- reservation.room_id → Room.id
- payment.reservation_id → Reservation.id
- review.reservation_id → Reservation.id
- hotel_amenity.(hotel_id, amenity_id) → Hotel.id, Amenity.id
