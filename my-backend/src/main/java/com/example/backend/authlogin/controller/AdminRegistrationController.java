package com.example.backend.authlogin.controller;

import com.example.backend.authlogin.domain.User;
import com.example.backend.dto.ApiResponse;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminRegistrationController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public static record AdminRegisterRequest(
            String name,
            String email,
            String password,
            String phone,
            String address,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth
    ) {}

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody AdminRegisterRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Email already in use"));
        }
        User u = User.builder()
                .name(req.name())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .provider(User.Provider.LOCAL)
                .profileImageUrl(null)
                .build();
        u.setPhone(req.phone());
        u.setAddress(req.address());
        u.setDateOfBirth(req.dateOfBirth());
        u.setRole(User.Role.ADMIN);
        u.setActive(true);
        return ResponseEntity.ok(ApiResponse.ok(userRepository.save(u)));
    }
}
