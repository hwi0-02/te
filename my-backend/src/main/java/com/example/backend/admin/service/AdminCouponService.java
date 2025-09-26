package com.example.backend.admin.service;

import com.example.backend.admin.domain.Coupon;
import com.example.backend.admin.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCouponService {
    private final CouponRepository couponRepository;

    public Page<Coupon> list(Boolean active, Coupon.DiscountType discountType, String code, String name, Pageable pageable) { 
        return couponRepository.search(active, discountType, code, name, pageable); 
    }
    
    public java.util.Map<String, Long> getStats() {
        Long total = couponRepository.count();
        Long active = couponRepository.countActiveCoupons();
        Long inactive = couponRepository.countInactiveCoupons();
        Long expired = couponRepository.countExpiredCoupons();
        
        return java.util.Map.of(
            "totalCoupons", total,
            "activeCoupons", active,
            "inactiveCoupons", inactive,
            "expiredCoupons", expired
        );
    }
    public Coupon get(Long id) { return couponRepository.findById(id).orElseThrow(); }
    public Coupon create(Coupon c) {
        if (couponRepository.existsByCode(c.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        return couponRepository.save(c);
    }
    public Coupon update(Long id, Coupon req) {
        Coupon c = couponRepository.findById(id).orElseThrow();
        if (req.getCode() != null && !req.getCode().equals(c.getCode())) {
            if (couponRepository.existsByCodeAndIdNot(req.getCode(), id)) {
                throw new IllegalArgumentException("Coupon code already exists");
            }
        }
        c.setName(req.getName());
        c.setCode(req.getCode());
        c.setDiscountType(req.getDiscountType());
        c.setDiscountValue(req.getDiscountValue());
        c.setMinSpend(req.getMinSpend());
        c.setValidFrom(req.getValidFrom());
        c.setValidTo(req.getValidTo());
        c.setIsActive(req.getIsActive());
        return couponRepository.save(c);
    }
    public void delete(Long id) { couponRepository.deleteById(id); }

    @Transactional
    public List<Coupon> bulkIssue(Long userId, String baseCode, int count, Coupon.DiscountType type,
                                  int discountValue, int minSpend, LocalDateTime validFrom, LocalDateTime validTo, boolean active) {
        List<Coupon> created = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String code = baseCode + "-" + i;
            if (couponRepository.existsByCode(code)) {
                // skip duplicates to keep operation idempotent-ish
                continue;
            }
            Coupon c = new Coupon();
            c.setUserId(userId);
            c.setName(baseCode + " " + i);
            c.setCode(code);
            c.setDiscountType(type);
            c.setDiscountValue(discountValue);
            c.setMinSpend(minSpend);
            c.setValidFrom(validFrom);
            c.setValidTo(validTo);
            c.setIsActive(active);
            created.add(couponRepository.save(c));
        }
        return created;
    }
}
