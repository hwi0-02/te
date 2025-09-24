package com.example.backend.admin.controller;

import com.example.backend.admin.repository.PaymentRepository;
import com.example.backend.admin.repository.ReservationRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminStatsController {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> dashboard() {
        Map<String,Object> m = new HashMap<>();
        m.put("totalUsers", userRepository.count());
        m.put("totalHotels", hotelRepository.count());
        m.put("totalReservations", reservationRepository.count());
        m.put("totalRevenue", paymentRepository.findAll().stream().mapToLong(p -> p.getTotalPrice() == null ? 0 : p.getTotalPrice()).sum());
        return ResponseEntity.ok(m);
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String,Object>> sales(@RequestParam(defaultValue = "monthly") String period,
                                                    @RequestParam(required = false) Integer year,
                                                    @RequestParam(required = false) Integer month) {
        Map<String,Object> m = new HashMap<>();
        m.put("period", period);
        m.put("year", year);
        m.put("month", month);
        var monthly = paymentRepository.sumMonthlyRevenue(year);
        m.put("monthlyData", monthly);
        long totalRevenue = monthly.stream().mapToLong(r -> ((Number) r[1]).longValue()).sum();
        m.put("totalRevenue", totalRevenue);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String,Object>> userStats(@RequestParam int year) {
        Map<String,Object> m = new HashMap<>();
        List<Object[]> rows = userRepository.countMonthlyUsers(year);
        m.put("year", year);
        m.put("monthly", rows);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/reservations")
    public ResponseEntity<Map<String,Object>> reservationStats(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Map<String,Object> m = new HashMap<>();
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        long count = reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null && !r.getCreatedAt().isBefore(start) && !r.getCreatedAt().isAfter(end))
                .count();
        m.put("from", from);
        m.put("to", to);
        m.put("count", count);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/hotels/ranking")
    public ResponseEntity<Map<String,Object>> hotelRanking(@RequestParam(required = false) Integer year) {
        Map<String,Object> m = new HashMap<>();
        var rows = paymentRepository.hotelRevenueByYear(year);
        m.put("year", year);
        m.put("ranking", rows);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/cancellation-reasons")
    public ResponseEntity<Map<String,Object>> cancellationReasons() {
        Map<String,Object> m = new HashMap<>();
        // Placeholder: no cancellation reason column in schema; return empty
        m.put("reasons", List.of());
        return ResponseEntity.ok(m);
    }
}
