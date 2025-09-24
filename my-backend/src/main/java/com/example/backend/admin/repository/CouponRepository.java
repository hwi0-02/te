package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT c FROM Coupon c WHERE (:active IS NULL OR c.isActive = :active)")
    Page<Coupon> search(@Param("active") Boolean active, Pageable pageable);
}
