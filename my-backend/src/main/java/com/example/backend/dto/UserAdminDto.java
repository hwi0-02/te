package com.example.backend.dto;

import com.example.backend.authlogin.domain.User;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserAdminDto {
    Long id;
    String name;
    String email;
    String phone;
    String role;

    public static UserAdminDto from(User u) {
        return UserAdminDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole() != null ? u.getRole().name() : null)
                .build();
    }
}
