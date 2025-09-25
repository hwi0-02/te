package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	@Query("SELECT p FROM Payment p WHERE (:status IS NULL OR p.status = :status) " +
			"AND (:from IS NULL OR p.createdAt >= :from) AND (:to IS NULL OR p.createdAt <= :to) " +
			"AND (:refundFrom IS NULL OR (p.refundedAt IS NOT NULL AND p.refundedAt >= :refundFrom)) " +
			"AND (:refundTo IS NULL OR (p.refundedAt IS NOT NULL AND p.refundedAt <= :refundTo))")
	Page<Payment> search(@Param("status") Payment.Status status,
	                   @Param("from") LocalDateTime from,
	                   @Param("to") LocalDateTime to,
	                   @Param("refundFrom") LocalDateTime refundFrom,
	                   @Param("refundTo") LocalDateTime refundTo,
	                   Pageable pageable);

	@Query(value = "SELECT MONTH(created_at) AS m, COALESCE(SUM(total_price),0) AS revenue, COUNT(*) AS cnt " +
			"FROM payment WHERE (:year IS NULL OR YEAR(created_at)=:year) GROUP BY m ORDER BY m", nativeQuery = true)
	java.util.List<Object[]> sumMonthlyRevenue(@Param("year") Integer year);

	@Query(value = "SELECT h.id AS hotel_id, h.name AS hotel_name, COALESCE(SUM(p.total_price),0) AS revenue " +
			"FROM hotel h " +
			"JOIN room r ON r.hotel_id = h.id " +
			"JOIN reservation res ON res.room_id = r.id " +
			"JOIN payment p ON p.reservation_id = res.id AND p.status = 'PAID' " +
			"WHERE (:year IS NULL OR YEAR(p.created_at) = :year) " +
			"GROUP BY h.id, h.name ORDER BY revenue DESC", nativeQuery = true)
	java.util.List<Object[]> hotelRevenueByYear(@Param("year") Integer year);
	
	@Query("SELECT COALESCE(SUM(p.totalPrice), 0) FROM Payment p WHERE p.createdAt BETWEEN :from AND :to AND p.status = 'PAID'")
	Long sumTotalPriceByCreatedAtBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	// 최근 N일 일별 매출 (빈 날짜는 FE에서 0 채움)
	@Query(value = "SELECT DATE(p.created_at) as d, COALESCE(SUM(p.total_price),0) as revenue " +
			"FROM payment p WHERE p.status='PAID' AND p.created_at BETWEEN :from AND :to " +
			"GROUP BY d ORDER BY d", nativeQuery = true)
	java.util.List<Object[]> dailyRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	// 연도별 호텔 매출 TOP 순위 (limit 는 자바측에서 subList 적용)
	@Query(value = "SELECT h.id, h.name, COALESCE(SUM(p.total_price),0) AS revenue " +
			"FROM hotel h " +
			"JOIN room r ON r.hotel_id = h.id " +
			"JOIN reservation res ON res.room_id = r.id " +
			"JOIN payment p ON p.reservation_id = res.id AND p.status='PAID' " +
			"WHERE (:year IS NULL OR YEAR(p.created_at)=:year) " +
			"GROUP BY h.id, h.name ORDER BY revenue DESC", nativeQuery = true)
	java.util.List<Object[]> topHotelRevenue(@Param("year") Integer year);
}
