package com.example.backend.admin.controller;

import com.example.backend.admin.service.AdminHotelService;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import com.example.backend.fe_hotel_detail.domain.Hotel;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/hotels")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminHotelController {

    private final AdminHotelService hotelService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Hotel>>> list(@RequestParam(required = false) String name,
                                            @RequestParam(required = false) Integer minStar,
                                            @RequestParam(required = false) Hotel.ApprovalStatus status,
                                            Pageable pageable) {
        Page<Hotel> page;
        if (status != null) {
            page = hotelService.list(name, minStar, status, pageable);
        } else {
            page = hotelService.list(name, minStar, null, pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Hotel>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(hotelService.get(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        hotelService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id, @RequestParam(required = false) String note) {
        // TODO: 인증 컨텍스트에서 adminUserId 추출
        Long adminUserId = null;
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String) {
            String email = (String) auth.getPrincipal();
            var userOpt = userRepository.findByEmail(email);
            adminUserId = userOpt.map(u -> u.getId()).orElse(null);
        }
        hotelService.approve(id, adminUserId, note);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam(required = false) String reason) {
        hotelService.reject(id, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable Long id, @RequestParam(required = false) String reason) {
        hotelService.suspend(id, reason);
        return ResponseEntity.ok().build();
    }
}
