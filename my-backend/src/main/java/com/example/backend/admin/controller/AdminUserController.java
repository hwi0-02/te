package com.example.backend.admin.controller;

import com.example.backend.admin.service.AdminUserService;
import com.example.backend.authlogin.domain.User;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import com.example.backend.dto.RoleUpdateRequest;
import com.example.backend.dto.UserAdminDto;
import com.example.backend.dto.UserStatsDto;
import com.example.backend.dto.UserStatusUpdateRequest;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminUserController {

    private final AdminUserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserAdminDto>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) User.Role role,
            Pageable pageable) {
        var page = userService.list(name, email, role, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminDto>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.get(id)));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id, @RequestBody RoleUpdateRequest req) {
        userService.updateRole(id, req.getRole());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id, @RequestBody UserStatusUpdateRequest req) {
        userService.updateStatus(id, req.isActive());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsDto>> stats() {
        long total = userRepository.count();
        long admins = userRepository.countByRole(User.Role.ADMIN);
        long business = userRepository.countByRole(User.Role.BUSINESS);
        long users = userRepository.countByRole(User.Role.USER);
        return ResponseEntity.ok(ApiResponse.ok(new UserStatsDto(total, admins, business, users)));
    }
}
