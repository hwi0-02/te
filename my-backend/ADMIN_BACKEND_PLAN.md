# 호텔 예약 시스템 - 관리자 백엔드 개발 계획서

## 📋 프로젝트 개요
- **프로젝트명**: 호텔 예약 시스템 관리자 백엔드
- **기술 스택**: Spring Boot 3.5.5, JPA, MariaDB, Spring Security
- **현재 상태**: 프론트엔드(Vue.js) 완료, 백엔드 개발 필요

## 🎯 관리자 기능 요구사항 (admin.md 기준)

### 1. 전체 회원 관리 (모든 호텔 이용자, 호텔 소유자)
### 2. 전체 호텔 승인/정지/삭제 관리 (호텔 소유자가 요청한 등록 승인)
### 3. 전체 매출, 수수료 관리 (호텔별 정산, 플랫폼 수익 확인)
### 4. 전체 예약/결제 현황 모니터링 및 관리
### 5. 전체 리뷰 관리 (호텔 리뷰 신고 접수 및 조치)
### 6. 쿠폰/광고/프로모션 관리 (배너, 광고, 공지사항)
### 7. 관리자 계정 권한 관리 (서브 관리자, 운영 직원 추가)
### 8. 고객센터 관리 (FAQ, 문의 관리)
### 9. 그래프(매출, 가입자 수, 예약 수, 취소 사유, 최다 예약 호텔 등수 등)

## 🗂️ 백엔드 패키지 구조 계획

```
com.example.backend.admin/
├── controller/          # REST API 컨트롤러
│   ├── AdminUserController.java
│   ├── AdminHotelController.java
│   ├── AdminReservationController.java
│   ├── AdminPaymentController.java
│   ├── AdminReviewController.java
│   ├── AdminCouponController.java
│   ├── AdminNoticeController.java
│   ├── AdminDashboardController.java
│   └── AdminStatsController.java
├── service/             # 비즈니스 로직
│   ├── AdminUserService.java
│   ├── AdminHotelService.java
│   ├── AdminReservationService.java
│   ├── AdminPaymentService.java
│   ├── AdminReviewService.java
│   ├── AdminCouponService.java
│   ├── AdminNoticeService.java
│   ├── AdminDashboardService.java
│   └── AdminStatsService.java
├── dto/                 # 데이터 전송 객체
│   ├── request/
│   └── response/
├── entity/              # JPA 엔티티 (기존 테이블 매핑)
└── repository/          # JPA 리포지토리
```

## 📊 데이터베이스 테이블 분석 및 추가 필요 테이블

### 기존 테이블 (schema.sql 기준)
- ✅ `app_user` - 사용자 정보
- ✅ `Hotel` - 호텔 정보
- ✅ `hotel_image` - 호텔 이미지
- ✅ `Amenity` - 편의시설
- ✅ `Hotel_Amenity` - 호텔-편의시설 매핑
- ✅ `Room` - 객실 정보
- ✅ `room_image` - 객실 이미지
- ✅ `Room_Inventory` - 객실 재고
- ✅ `Room_Price_Policy` - 객실 요금 정책
- ✅ `Reservation` - 예약 정보
- ✅ `Payment` - 결제 정보
- ✅ `Coupon` - 쿠폰 정보
- ✅ `Review` - 리뷰 정보

### 추가 필요 테이블
-- 호텔 승인 상태 필드 추가 (Hotel 테이블 수정)
ALTER TABLE `Hotel` ADD COLUMN `approval_status` ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') NOT NULL DEFAULT 'PENDING';
ALTER TABLE `Hotel` ADD COLUMN `approval_date` TIMESTAMP NULL;
ALTER TABLE `Hotel` ADD COLUMN `approved_by` BIGINT NULL;
ALTER TABLE `Hotel` ADD COLUMN `rejection_reason` TEXT NULL;



## 🚀 개발 단계별 계획

### Phase 1: 기본 인프라 구축 (1주차)

#### 1.1 Entity 및 Repository 개발
- [x] 기존 테이블 JPA Entity 매핑 (User/Hotel/Room/Reservation/Payment/Review/Coupon 일부 완료)
- [x] 추가 테이블 Entity 생성 (Amenity/Hotel_Amenity/Room_Inventory/Room_Price_Policy)
- [x] Repository 인터페이스 구현 (핵심 테이블 + 보조 테이블)
- [ ] 기본 CRUD 메서드 정의 (서비스 계층 연동 시 보강)

#### 1.2 Security 및 인증/인가 설정
- [x] 관리자 권한 체크 로직 구현 (ROLE_ADMIN 필요)
- [x] JWT 토큰 기반 인증 (필터 연동 및 ROLE 주입)
- [x] 관리자 전용 API 보안 설정 (/api/admin/**)

#### 1.3 기본 DTO 및 공통 응답 구조
- [x] 공통 응답 포맷 정의 (ApiResponse)
- [ ] 페이징 처리 DTO
- [x] 기본 Request/Response DTO (UserAdminDto, RoleUpdateRequest, DashboardDto)

### Phase 2: 핵심 관리 기능 구현 (2주차)

#### 2.1 회원 관리 기능
```java
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    // GET /api/admin/users - 전체 회원 목록 (페이징, 검색, 필터)
    // GET /api/admin/users/{id} - 회원 상세 정보
    // PUT /api/admin/users/{id}/status - 회원 상태 변경 (활성/비활성)
    // DELETE /api/admin/users/{id} - 회원 삭제
    // GET /api/admin/users/stats - 회원 통계
}
```

#### 2.2 호텔 관리 기능
```java
@RestController
@RequestMapping("/api/admin/hotels")
public class AdminHotelController {
    // GET /api/admin/hotels - 전체 호텔 목록 (승인상태별 필터)
    // GET /api/admin/hotels/{id} - 호텔 상세 정보
    // PUT /api/admin/hotels/{id}/approve - 호텔 승인
    // PUT /api/admin/hotels/{id}/reject - 호텔 거부
    // PUT /api/admin/hotels/{id}/suspend - 호텔 정지
    // DELETE /api/admin/hotels/{id} - 호텔 삭제
}
```

#### 2.3 예약/결제 관리 기능
```java
@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    // GET /api/admin/reservations - 전체 예약 목록
    // GET /api/admin/reservations/{id} - 예약 상세 정보
    // PUT /api/admin/reservations/{id}/cancel - 예약 취소
    // GET /api/admin/payments - 결제 내역 관리
    // PUT /api/admin/payments/{id}/refund - 환불 처리
}
```

#### 2.4 리뷰 관리 기능
```java
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {
    // GET /api/admin/reviews - 전체 리뷰 목록
    // GET /api/admin/reviews/reports - 신고된 리뷰 목록
    // PUT /api/admin/reviews/{id}/hide - 리뷰 숨김
    // PUT /api/admin/reviews/reports/{id}/resolve - 신고 처리
    // DELETE /api/admin/reviews/{id} - 리뷰 삭제
}
```

### Phase 3: 고급 기능 및 대시보드 (3주차)

#### 3.1 쿠폰/프로모션 관리
```java
@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {
    // GET /api/admin/coupons - 쿠폰 목록
    // POST /api/admin/coupons - 쿠폰 생성
    // PUT /api/admin/coupons/{id} - 쿠폰 수정
    // DELETE /api/admin/coupons/{id} - 쿠폰 삭제
    // POST /api/admin/coupons/bulk-issue - 대량 쿠폰 발급
}
```

#### 3.2 고객센터 관리
```java
@RestController
@RequestMapping("/api/admin/cs")
public class AdminCSController {
    // GET /api/admin/inquiries - 문의 목록
    // PUT /api/admin/inquiries/{id}/reply - 문의 답변
    // GET /api/admin/notices - 공지사항 관리
    // POST /api/admin/notices - 공지사항 등록
}
```

#### 3.3 통계 및 대시보드
```java
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {
    // GET /api/admin/stats/dashboard - 대시보드 요약
    // GET /api/admin/stats/sales - 매출 통계
    // GET /api/admin/stats/users - 사용자 통계
    // GET /api/admin/stats/reservations - 예약 통계
    // GET /api/admin/stats/hotels/ranking - 호텔 순위
    // GET /api/admin/stats/cancellation-reasons - 취소 사유 분석
}
```

## 📈 주요 API 명세서

### 1. 대시보드 통계 API
```json
GET /api/admin/stats/dashboard
Response:
{
  "totalUsers": 1250,
  "totalHotels": 89,
  "totalReservations": 3420,
  "totalRevenue": 125000000,
  "monthlyStats": {
    "newUsers": 45,
    "newReservations": 234,
    "revenue": 12500000,
    "cancellationRate": 8.5
  },
  "topHotels": [...],
  "recentActivities": [...]
}
```

### 2. 호텔 승인 API
```json
PUT /api/admin/hotels/{id}/approve
Request:
{
  "approvalNote": "승인 사유"
}
Response:
{
  "success": true,
  "message": "호텔이 승인되었습니다.",
  "data": {
    "hotelId": 123,
    "status": "APPROVED",
    "approvedAt": "2025-09-24T10:30:00Z"
  }
}
```

### 3. 매출 통계 API
```json
GET /api/admin/stats/sales?period=monthly&year=2025
Response:
{
  "period": "monthly",
  "year": 2025,
  "totalRevenue": 1250000000,
  "platformCommission": 125000000,
  "monthlyData": [
    {
      "month": 1,
      "revenue": 85000000,
      "commission": 8500000,
      "reservationCount": 1200
    }
  ],
  "topRevenueHotels": [...]
}
```

## 🔧 개발 시 주의사항

### 1. 데이터베이스 관련
- 기존 schema.sql의 테이블명이 Pascal Case (`Hotel`, `Room` 등)이므로 JPA Entity에서 `@Table(name = "Hotel")` 명시적 지정
- 외래키 제약조건 고려한 삭제 로직 구현
- 대용량 데이터 처리를 위한 페이징 필수 적용

### 2. 보안 관련
- 관리자 권한 체크를 모든 API에 적용
- 민감한 정보 (결제정보, 개인정보) 접근 시 추가 인증
- API 호출 로그 기록

### 3. 성능 관련
- 통계 데이터는 캐싱 적용 고려
- N+1 문제 방지를 위한 Fetch Join 활용
- 대시보드 API는 비동기 처리 고려

## 📋 테스트 계획
- [ ] 단위 테스트 (Service Layer)
- [ ] 통합 테스트 (Controller Layer)
- [ ] 보안 테스트 (권한 체크)
- [ ] 성능 테스트 (대용량 데이터 처리)

## 🚀 배포 및 운영
- [ ] 프로덕션 환경 설정
- [ ] 로깅 및 모니터링 설정
- [ ] API 문서화 (Swagger)
- [ ] 관리자 매뉴얼 작성

---

이 계획서를 바탕으로 단계별로 개발을 진행하시면 됩니다. 각 단계별로 구체적인 구현이 필요하시면 언제든지 말씀해 주세요!