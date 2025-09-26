package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Payment;
import com.example.backend.admin.service.AdminPaymentService;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import com.example.backend.admin.dto.PaymentSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminPaymentController {
    private final AdminPaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentSummaryDto>>> list(@RequestParam(required = false) Payment.Status status,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                  @RequestParam(required = false) String hotelName,
                                                                  @RequestParam(required = false) String userName,
                                                                  Pageable pageable) {
        log.info("결제 목록 API 호출 - status: {}, from: {}, to: {}, hotelName: {}, userName: {}", 
                status, from, to, hotelName, userName);
        
        var page = paymentService.listWithDetails(status, from, to, hotelName, userName, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.get(id)));
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Void>> refund(@PathVariable Long id) {
        paymentService.refund(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
