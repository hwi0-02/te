package com.example.backend.admin.service;

import com.example.backend.admin.domain.Payment;
import com.example.backend.admin.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminPaymentService {
    private final PaymentRepository paymentRepository;

    public Page<Payment> list(Payment.Status status, LocalDateTime from, LocalDateTime to,
                              LocalDateTime refundFrom, LocalDateTime refundTo,
                              Pageable pageable) {
        return paymentRepository.search(status, from, to, refundFrom, refundTo, pageable);
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
}
