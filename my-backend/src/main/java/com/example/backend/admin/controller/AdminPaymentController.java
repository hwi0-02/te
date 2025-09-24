package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Payment;
import com.example.backend.admin.service.AdminPaymentService;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminPaymentController {
    private final AdminPaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Payment>>> list(@RequestParam(required = false) Payment.Status status,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                  @RequestParam(required = false, name = "refundFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime refundFrom,
                                                                  @RequestParam(required = false, name = "refundTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime refundTo,
                                                                  Pageable pageable) {
        var page = paymentService.list(status, from, to, refundFrom, refundTo, pageable);
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
