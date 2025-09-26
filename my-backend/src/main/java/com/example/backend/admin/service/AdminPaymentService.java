package com.example.backend.admin.service;

import com.example.backend.admin.domain.Payment;
import com.example.backend.admin.repository.PaymentRepository;
import com.example.backend.admin.dto.PaymentSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPaymentService {
    private final PaymentRepository paymentRepository;

    public Page<Payment> list(Payment.Status status, LocalDateTime from, LocalDateTime to,
                              LocalDateTime refundFrom, LocalDateTime refundTo,
                              Pageable pageable) {
        return paymentRepository.search(status, from, to, refundFrom, refundTo, pageable);
    }

    // 상세 정보 포함 결제 목록 조회
    public Page<PaymentSummaryDto> listWithDetails(Payment.Status status, LocalDateTime from, LocalDateTime to,
                                                   String hotelName, String userName,
                                                   Pageable pageable) {
        String statusStr = status != null ? status.name() : null;
        
        log.info("결제 목록 조회 - status: {}, from: {}, to: {}, hotelName: {}, userName: {}", 
                statusStr, from, to, hotelName, userName);

        Page<Object[]> results = paymentRepository.searchWithDetails(statusStr, from, to, hotelName, userName, pageable);
        
        log.info("조회된 결제 결과 수: {}", results.getContent().size());

        List<PaymentSummaryDto> dtos = results.getContent().stream()
            .map(this::mapToPaymentSummaryDto)
            .toList();

        return new PageImpl<>(dtos, pageable, results.getTotalElements());
    }

    public Payment get(Long id) { return paymentRepository.findById(id).orElseThrow(); }

    public void refund(Long id) {
        Payment p = paymentRepository.findById(id).orElseThrow();
        if (p.getStatus() == Payment.Status.PAID) {
            p.setStatus(Payment.Status.REFUNDED);
            p.setRefundedAt(LocalDateTime.now());
            paymentRepository.save(p);
        }
    }

    private PaymentSummaryDto mapToPaymentSummaryDto(Object[] row) {
        if (row == null || row.length < 11) {
            log.warn("결제 데이터 매핑 오류: 최소 11개 필드가 필요하지만 {}개만 조회됨", row != null ? row.length : 0);
            throw new IllegalArgumentException("조회된 결제 데이터가 불완전합니다");
        }

        return PaymentSummaryDto.builder()
            .paymentId(safeLong(row[0]))
            .reservationId(safeLong(row[1]))
            .transactionId(safeString(row[2]))
            .hotelName(safeString(row[3]))
            .userName(safeString(row[4]))
            .totalPrice(safeInteger(row[6]))
            .paymentMethod(safeString(row[7]))
            .paymentStatus(safeString(row[8]))
            .paymentCreatedAt(safeDateTime(row[9]))
            .refundedAt(safeDateTime(row[10]))
            .build();
    }

    private Long safeLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        return null;
    }

    private Integer safeInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return null;
    }

    private String safeString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private LocalDateTime safeDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        return null;
    }
}
