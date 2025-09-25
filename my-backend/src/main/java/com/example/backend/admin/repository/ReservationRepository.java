package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	@Query("SELECT r FROM Reservation r WHERE (:status IS NULL OR r.status = :status) " +
			"AND (:from IS NULL OR r.createdAt >= :from) AND (:to IS NULL OR r.createdAt <= :to) " +
			"AND (:stayFrom IS NULL OR r.endDate >= :stayFrom) AND (:stayTo IS NULL OR r.startDate <= :stayTo)")
	Page<Reservation> search(@Param("status") Reservation.Status status,
	                        @Param("from") LocalDateTime from,
	                        @Param("to") LocalDateTime to,
	                        @Param("stayFrom") LocalDateTime stayFrom,
	                        @Param("stayTo") LocalDateTime stayTo,
	                        Pageable pageable);
	
	long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
