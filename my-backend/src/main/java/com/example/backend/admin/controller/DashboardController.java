package com.example.backend.admin.controller;

import com.example.backend.admin.repository.PaymentRepository;
import com.example.backend.admin.repository.ReservationRepository;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
	private final UserRepository userRepository;
	private final HotelRepository hotelRepository;
	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;

	@GetMapping("/quick-summary")
	public ResponseEntity<Map<String,Object>> quickSummary() {
		Map<String,Object> m = new HashMap<>();
		m.put("users", userRepository.count());
		m.put("hotels", hotelRepository.count());
		m.put("reservations", reservationRepository.count());
		m.put("payments", paymentRepository.count());
		return ResponseEntity.ok(m);
	}
}