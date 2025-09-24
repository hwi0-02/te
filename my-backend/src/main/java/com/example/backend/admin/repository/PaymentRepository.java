package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	@Query("SELECT p FROM Payment p WHERE (:status IS NULL OR p.status = :status) " +
			"AND (:from IS NULL OR p.createdAt >= :from) AND (:to IS NULL OR p.createdAt <= :to) " +
			"AND (:refundFrom IS NULL OR (p.refundedAt IS NOT NULL AND p.refundedAt >= :refundFrom)) " +
			"AND (:refundTo IS NULL OR (p.refundedAt IS NOT NULL AND p.refundedAt <= :refundTo))")
	Page<Payment> search(@Param("status") Payment.Status status,
	                   @Param("from") java.time.LocalDateTime from,
	                   @Param("to") java.time.LocalDateTime to,
	                   @Param("refundFrom") java.time.LocalDateTime refundFrom,
	                   @Param("refundTo") java.time.LocalDateTime refundTo,
	                   Pageable pageable);

	@Query(value = "SELECT MONTH(created_at) AS m, COALESCE(SUM(total_price),0) AS revenue, COUNT(*) AS cnt " +
			"FROM Payment WHERE (:year IS NULL OR YEAR(created_at)=:year) GROUP BY m ORDER BY m", nativeQuery = true)
	java.util.List<Object[]> sumMonthlyRevenue(@Param("year") Integer year);

	@Query(value = "SELECT h.id AS hotel_id, h.name AS hotel_name, COALESCE(SUM(p.total_price),0) AS revenue " +
			"FROM Hotel h " +
			"JOIN Room r ON r.hotel_id = h.id " +
			"JOIN Reservation res ON res.room_id = r.id " +
			"JOIN Payment p ON p.reservation_id = res.id AND p.status = 'PAID' " +
			"WHERE (:year IS NULL OR YEAR(p.created_at) = :year) " +
			"GROUP BY h.id, h.name ORDER BY revenue DESC", nativeQuery = true)
	java.util.List<Object[]> hotelRevenueByYear(@Param("year") Integer year);
}
