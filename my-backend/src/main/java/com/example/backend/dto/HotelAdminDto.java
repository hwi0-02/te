package com.example.backend.dto;

import com.example.backend.fe_hotel_detail.domain.Hotel;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class HotelAdminDto {
    Long id;
    String name;
    String address;
    String description;
    String country;
    Integer starRating;
    String status; // approval_status를 string으로
    LocalDateTime approvalDate;
    Long approvedBy;
    String rejectionReason;
    
    // 추가 정보 (조인 또는 계산으로 얻는 값들)
    String businessName;
    String businessEmail;
    String city;
    Integer roomCount;
    Integer reservationCount;
    Double averageRating;
    Long totalRevenue;
    LocalDateTime createdAt;

    public static HotelAdminDto from(Hotel hotel) {
        return HotelAdminDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .description(hotel.getDescription())
                .country(hotel.getCountry())
                .starRating(hotel.getStarRating())
                .status(hotel.getApprovalStatus() != null ? hotel.getApprovalStatus().name() : "PENDING")
                .approvalDate(hotel.getApprovalDate())
                .approvedBy(hotel.getApprovedBy())
                .rejectionReason(hotel.getRejectionReason())
                // 기본값 설정 (실제로는 서비스에서 조인/계산해서 설정)
                .businessName("N/A")
                .businessEmail("N/A")
                .city(extractCity(hotel.getAddress()))
                .roomCount(0)
                .reservationCount(0)
                .averageRating(0.0)
                .totalRevenue(0L)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    private static String extractCity(String address) {
        if (address == null) return "N/A";
        // 간단한 도시 추출 로직 (예: "서울 강남구 언주로 640" -> "서울")
        String[] parts = address.split(" ");
        return parts.length > 0 ? parts[0] : "N/A";
    }
}
