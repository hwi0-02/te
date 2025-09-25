package com.example.backend.admin.controller;

import com.example.backend.admin.repository.InquiryRepository;
import com.example.backend.admin.repository.PaymentRepository;
import com.example.backend.admin.repository.ReservationRepository;
import com.example.backend.admin.repository.ReviewRepository;
import com.example.backend.admin.repository.CouponRepository;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.DashboardDto;
import com.example.backend.dto.DashboardDetailDto;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final InquiryRepository inquiryRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDto>> summary() {
        // 오늘 데이터
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        // 최근 7일 매출
        LocalDateTime weekAgo = now.minusDays(7);
        Long recentRevenue = paymentRepository.sumTotalPriceByCreatedAtBetween(weekAgo, now);
        
    long totalUsers = userRepository.count();
    long businessUsers = userRepository.countByRole(com.example.backend.authlogin.domain.User.Role.BUSINESS);
    long totalReviews = reviewRepository.count();
    long totalCoupons = couponRepository.count();

    // 총 매출 (전체 기간) - 성능 고려해서 별도 인덱스 없으면 필요한 시점에만 사용하거나 캐시 고려
    Long allRevenue = paymentRepository.sumTotalPriceByCreatedAtBetween(LocalDateTime.now().minusYears(50), now); // 대략 전체

    DashboardDto dto = DashboardDto.builder()
        .totalUsers(totalUsers)
        .totalBusinesses(businessUsers)
        .totalHotels(hotelRepository.count())
        .totalReservations(reservationRepository.count())
        .totalPayments(paymentRepository.count())
        .totalReviews(totalReviews)
        .totalCoupons(totalCoupons)
        .pendingInquiries(inquiryRepository.countByStatus(com.example.backend.admin.domain.Inquiry.Status.PENDING))
        .recentRevenue(recentRevenue != null ? recentRevenue : 0L)
        .todayReservations(reservationRepository.countByCreatedAtBetween(startOfDay, now))
        .totalRevenue(allRevenue != null ? allRevenue : 0L)
        .build();
        
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 기본 카운트
    stats.put("totalUsers", userRepository.count());
    stats.put("totalBusinesses", userRepository.countByRole(com.example.backend.authlogin.domain.User.Role.BUSINESS));
        stats.put("totalHotels", hotelRepository.count());
        stats.put("totalReservations", reservationRepository.count());
        stats.put("totalPayments", paymentRepository.count());
    stats.put("totalReviews", reviewRepository.count());
    stats.put("totalCoupons", couponRepository.count());
        stats.put("pendingInquiries", inquiryRepository.countByStatus(com.example.backend.admin.domain.Inquiry.Status.PENDING));
        
        // 오늘 데이터
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        stats.put("todayReservations", reservationRepository.countByCreatedAtBetween(startOfDay, now));
        
        // 최근 7일 매출
        LocalDateTime weekAgo = now.minusDays(7);
        Long recentRevenue = paymentRepository.sumTotalPriceByCreatedAtBetween(weekAgo, now);
    stats.put("recentRevenue", recentRevenue != null ? recentRevenue : 0L);
    Long allRevenue2 = paymentRepository.sumTotalPriceByCreatedAtBetween(LocalDateTime.now().minusYears(50), now);
    stats.put("totalRevenue", allRevenue2 != null ? allRevenue2 : 0L);
        
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<DashboardDetailDto>> details(Integer days, Integer top, Integer year) {
        int periodDays = (days == null || days < 1 || days > 60) ? 14 : days; // default 14
        int topLimit = (top == null || top < 1 || top > 50) ? 5 : top; // default 5
        int targetYear = (year == null) ? LocalDateTime.now().getYear() : year;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(periodDays - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Daily revenue rows
        var revenueRows = paymentRepository.dailyRevenue(from, now);
        // Daily signups rows
        var signupRows = userRepository.countDailyUsers(from, now);

        var dailyRevenue = new ArrayList<DashboardDetailDto.DailyPoint>();
        var dailySignup = new ArrayList<DashboardDetailDto.DailyPoint>();

        // Create maps for quick lookup
        java.util.Map<String, Long> revenueMap = new java.util.HashMap<>();
        for (Object[] row : revenueRows) {
            java.sql.Date d = (java.sql.Date) row[0];
            Number rev = (Number) row[1];
            revenueMap.put(d.toLocalDate().toString(), rev.longValue());
        }
        java.util.Map<String, Long> signupMap = new java.util.HashMap<>();
        for (Object[] row : signupRows) {
            java.sql.Date d = (java.sql.Date) row[0];
            Number cnt = (Number) row[1];
            signupMap.put(d.toLocalDate().toString(), cnt.longValue());
        }

        for (int i = 0; i < periodDays; i++) {
            var date = from.plusDays(i).toLocalDate();
            String key = date.toString();
            dailyRevenue.add(DashboardDetailDto.DailyPoint.builder().date(key).value(revenueMap.getOrDefault(key, 0L)).build());
            dailySignup.add(DashboardDetailDto.DailyPoint.builder().date(key).value(signupMap.getOrDefault(key, 0L)).build());
        }

        // Monthly signups
        var monthlyRows = userRepository.countMonthlyUsers(targetYear);
        var monthly = new ArrayList<DashboardDetailDto.MonthlyPoint>();
        int[] monthlyCounts = new int[13];
        for (Object[] r : monthlyRows) {
            Number m = (Number) r[0];
            Number c = (Number) r[1];
            monthlyCounts[m.intValue()] = c.intValue();
        }
        for (int m = 1; m <= 12; m++) {
            monthly.add(DashboardDetailDto.MonthlyPoint.builder().month(m).count(monthlyCounts[m]).build());
        }

        // Top hotels revenue
        var hotelRows = paymentRepository.topHotelRevenue(targetYear);
        var topHotels = new ArrayList<DashboardDetailDto.HotelRevenuePoint>();
        for (int i = 0; i < hotelRows.size() && i < topLimit; i++) {
            Object[] r = hotelRows.get(i);
            Long hotelId = ((Number) r[0]).longValue();
            String hotelName = (String) r[1];
            Number rev = (Number) r[2];
            topHotels.add(DashboardDetailDto.HotelRevenuePoint.builder()
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .revenue(rev.longValue())
                    .build());
        }

        var dto = DashboardDetailDto.builder()
                .dailyRevenue(dailyRevenue)
                .dailySignups(dailySignup)
                .monthlySignups(monthly)
                .topHotels(topHotels)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
