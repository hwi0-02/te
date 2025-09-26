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

	// 결제 상세 정보 조회 (예약, 호텔, 사용자 정보 포함)
	@Query(value = """
		SELECT
			p.id AS paymentId,
			p.reservation_id AS reservationId,
			r.transaction_id AS transactionId,
			h.name AS hotelName,
			u.name AS userName,
			p.total_price AS totalPrice,
			p.payment_method AS paymentMethod,
			p.status AS paymentStatus,
			p.created_at AS createdAt, -- "paymentCreatedAt"에서 "createdAt"으로 별칭 변경
			p.refunded_at AS refundedAt
		FROM payment p
		LEFT JOIN reservation r ON p.reservation_id = r.id
		LEFT JOIN room rm ON r.room_id = rm.id
		LEFT JOIN hotel h ON rm.hotel_id = h.id
		LEFT JOIN app_user u ON r.user_id = u.id
		WHERE (:status IS NULL OR p.status = :status)
		AND (:from IS NULL OR p.created_at >= :from)
		AND (:to IS NULL OR p.created_at <= :to)
		AND (:hotelName IS NULL OR h.name LIKE CONCAT('%', :hotelName, '%'))
		AND (:userName IS NULL OR u.name LIKE CONCAT('%', :userName, '%'))
		""",
		countQuery = """
		SELECT COUNT(*)
		FROM payment p
		LEFT JOIN reservation r ON p.reservation_id = r.id
		LEFT JOIN room rm ON r.room_id = rm.id
		LEFT JOIN hotel h ON rm.hotel_id = h.id
		LEFT JOIN app_user u ON r.user_id = u.id
		WHERE (:status IS NULL OR p.status = :status)
		AND (:from IS NULL OR p.created_at >= :from)
		AND (:to IS NULL OR p.created_at <= :to)
		AND (:hotelName IS NULL OR h.name LIKE CONCAT('%', :hotelName, '%'))
		AND (:userName IS NULL OR u.name LIKE CONCAT('%', :userName, '%'))
		""",
		nativeQuery = true)
	Page<Object[]> searchWithDetails(@Param("status") String status,
	                               @Param("from") LocalDateTime from,
	                               @Param("to") LocalDateTime to,
	                               @Param("hotelName") String hotelName,
	                               @Param("userName") String userName,
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
	
	// 결제 데이터 존재 여부 확인 (디버깅용)
	@Query("SELECT COUNT(p) FROM Payment p")
	Long countAllPayments();
	
	// 특정 예약의 결제 정보 조회
	@Query("SELECT p FROM Payment p WHERE p.reservationId = :reservationId")
	java.util.List<Payment> findByReservationId(@Param("reservationId") Long reservationId);

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

	// 호텔별 매출 (기간 필터)
	@Query(value = "SELECT h.id AS hotel_id, h.name AS hotel_name, COALESCE(SUM(p.total_price),0) AS revenue, " +
			"COUNT(DISTINCT res.id) AS reservation_count " +
			"FROM hotel h " +
			"JOIN room r ON r.hotel_id = h.id " +
			"JOIN reservation res ON res.room_id = r.id " +
			"JOIN payment p ON p.reservation_id = res.id AND p.status='PAID' " +
			"WHERE p.created_at BETWEEN :from AND :to " +
			"GROUP BY h.id, h.name ORDER BY revenue DESC", nativeQuery = true)
	java.util.List<Object[]> hotelRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}