package com.example.backend.admin.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.DashboardDto;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardDto>> summary() {
        DashboardDto dto = new DashboardDto(
                userRepository.count(),
                hotelRepository.count()
        );
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
