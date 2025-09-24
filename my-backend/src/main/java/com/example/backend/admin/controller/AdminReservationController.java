package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Reservation;
import com.example.backend.admin.service.AdminReservationService;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminReservationController {

    private final AdminReservationService reservationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Reservation>>> list(@RequestParam(required = false) Reservation.Status status,
                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                       @RequestParam(required = false, name = "stayFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime stayFrom,
                                                                       @RequestParam(required = false, name = "stayTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime stayTo,
                                                                       Pageable pageable) {
        var page = reservationService.list(status, from, to, stayFrom, stayTo, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Reservation>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reservationService.get(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
