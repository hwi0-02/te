package com.example.backend.fe_hotel_detail.repository;

import com.example.backend.fe_hotel_detail.domain.Hotel; // ← 이거여야 함
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query(value = "SELECT * FROM hotel h WHERE (:name IS NULL OR h.name LIKE CONCAT('%',:name,'%')) " +
	    "AND (:minStar IS NULL OR h.star_rating >= :minStar)",
	    countQuery = "SELECT COUNT(*) FROM hotel h WHERE (:name IS NULL OR h.name LIKE CONCAT('%',:name,'%')) " +
		    "AND (:minStar IS NULL OR h.star_rating >= :minStar)",
	    nativeQuery = true)
    Page<Hotel> search(@Param("name") String name, @Param("minStar") Integer minStar, Pageable pageable);

    @Query(value = "SELECT * FROM hotel h WHERE (:status IS NULL OR h.approval_status = :status) " +
	    "AND (:name IS NULL OR h.name LIKE CONCAT('%',:name,'%')) " +
	    "AND (:minStar IS NULL OR h.star_rating >= :minStar)",
	    countQuery = "SELECT COUNT(*) FROM hotel h WHERE (:status IS NULL OR h.approval_status = :status) " +
		    "AND (:name IS NULL OR h.name LIKE CONCAT('%',:name,'%')) " +
		    "AND (:minStar IS NULL OR h.star_rating >= :minStar)",
	    nativeQuery = true)
    Page<Hotel> searchByApproval(@Param("status") String status,
				 @Param("name") String name,
				 @Param("minStar") Integer minStar,
				 Pageable pageable);

    @Query(value = "SELECT h.id, h.name, bname, cnt, revenue, avg_rating FROM (\n" +
	    "  SELECT r.hotel_id as hotel_id, COUNT(res.id) as cnt, SUM(p.total_price) as revenue\n" +
	    "  FROM room r\n" +
	    "  JOIN reservation res ON res.room_id = r.id\n" +
	    "  LEFT JOIN payment p ON p.reservation_id = res.id\n" +
	    "  WHERE res.created_at BETWEEN :start AND :end\n" +
	    "  GROUP BY r.hotel_id\n" +
	    ") x\n" +
	    "JOIN hotel h ON h.id = x.hotel_id\n" +
	    "LEFT JOIN (SELECT reservation_id, AVG(star_rating) as avg_rating FROM review GROUP BY reservation_id) rv ON 1=1\n" +
	    "LEFT JOIN (SELECT id, name as bname FROM hotel) hb ON hb.id = h.id\n" +
	    "ORDER BY x.cnt DESC LIMIT 5",
	    nativeQuery = true)
    List<Object[]> getTopHotelsByReservations(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);
}
