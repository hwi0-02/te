package com.example.backend.admin.service;

import com.example.backend.authlogin.domain.User;
import com.example.backend.dto.HotelAdminDto;
import com.example.backend.fe_hotel_detail.domain.Hotel;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminHotelService {
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public Page<HotelAdminDto> list(String name, Integer minStar, Hotel.ApprovalStatus status, Pageable pageable) {
        // 간단한 목록 조회 (기본 메서드)
        Page<Hotel> hotels;
        if (status != null) {
            hotels = hotelRepository.searchByApproval(status.name(), name, minStar, pageable);
        } else if (name != null || minStar != null) {
            hotels = hotelRepository.search(name, minStar, pageable);
        } else {
            hotels = hotelRepository.findAll(pageable);
        }

        List<HotelAdminDto> dtos = hotels.stream().map(this::mapToSimpleHotelAdminDto).toList();
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, hotels.getTotalElements());
    }

    private HotelAdminDto mapToSimpleHotelAdminDto(Hotel hotel) {
        // 사업자 정보 조회
        String businessName = null;
        String businessEmail = null;
        String businessPhone = null;

        if (hotel.getUserId() != null) {
            var businessUser = userRepository.findById(hotel.getUserId());
            if (businessUser.isPresent()) {
                User user = businessUser.get();
                businessName = user.getName();
                businessEmail = user.getEmail();
                businessPhone = user.getPhone();
            }
        }

        return HotelAdminDto.from(
                hotel,
                businessName,
                businessEmail,
                businessPhone,
                0, // room_count - 간단 조회에서는 0으로 설정
                0, // reservation_count - 간단 조회에서는 0으로 설정
                0.0, // average_rating - 간단 조회에서는 0으로 설정
                0L   // total_revenue - 간단 조회에서는 0으로 설정
        );
    }

    public Page<HotelAdminDto> listWithBusinessInfo(String name, Integer minStar, Hotel.ApprovalStatus status, Pageable pageable) {
        List<Object[]> results = hotelRepository.findHotelsWithBusinessInfo(
                status != null ? status.name() : null,
                name, minStar);

        List<HotelAdminDto> dtos = results.stream().map(this::mapToHotelAdminDto).toList();
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, dtos.size());
    }

    private HotelAdminDto mapToHotelAdminDto(Object[] row) {
        return HotelAdminDto.from(
                // Hotel 정보 (0-9)
                Hotel.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .address((String) row[2])
                        .description((String) row[3])
                        .country((String) row[4])
                        .starRating(row[5] != null ? ((Number) row[5]).intValue() : null)
                        .approvalStatus(row[6] != null ? Hotel.ApprovalStatus.valueOf((String) row[6]) : null)
                        .approvalDate(row[7] == null ? null : (row[7] instanceof java.sql.Timestamp ts7 ? ts7.toLocalDateTime() : (java.time.LocalDateTime) row[7]))
                        .approvedBy(row[8] != null ? ((Number) row[8]).longValue() : null)
                        .rejectionReason((String) row[9])
                        .createdAt(row[10] == null ? null : (row[10] instanceof java.sql.Timestamp ts10 ? ts10.toLocalDateTime() : (java.time.LocalDateTime) row[10]))
                        .build(),
                // 사업자 정보 (11-13)
                (String) row[11], // business_name
                (String) row[12], // business_email
                (String) row[13], // business_phone
                // 통계 정보 (14-17)
                row[14] != null ? ((Number) row[14]).intValue() : 0, // room_count
                row[15] != null ? ((Number) row[15]).intValue() : 0, // reservation_count
                row[16] != null ? ((Number) row[16]).doubleValue() : 0.0, // average_rating
                row[17] != null ? ((Number) row[17]).longValue() : 0L  // total_revenue
        );
    }

    // 타입 변환 헬퍼 메서드 (안전한 변환, 중복 제거)
    private int toInt(Object[] arr, int idx) {
        Object v = (arr != null && idx < arr.length) ? arr[idx] : null;
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String s && !s.isBlank()) {
            try {
                return (int) Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
    private long toLong(Object[] arr, int idx) {
        Object v = (arr != null && idx < arr.length) ? arr[idx] : null;
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        if (v instanceof String s && !s.isBlank()) try { return (long) Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        return 0L;
    }
    private double toDouble(Object[] arr, int idx) {
        Object v = (arr != null && idx < arr.length) ? arr[idx] : null;
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s && !s.isBlank()) try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        return 0.0;
    }

    public HotelAdminDto get(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        Object[] stats = hotelRepository.findHotelStats(id);

        // 사업자 정보 조회
        String businessName = null;
        String businessEmail = null;
        String businessPhone = null;

        if (hotel.getUserId() != null) {
            var businessUser = userRepository.findById(hotel.getUserId());
            if (businessUser.isPresent()) {
                User user = businessUser.get();
                businessName = user.getName();
                businessEmail = user.getEmail();
                businessPhone = user.getPhone();
            }
        }

        return HotelAdminDto.from(
                hotel,
                businessName,
                businessEmail,
                businessPhone,
                toInt(stats, 0), // total_rooms
                toInt(stats, 1), // total_reservations
                toDouble(stats, 2), // average_rating
                toLong(stats, 3)  // total_revenue
        );
    }

    public void delete(Long id) {
        hotelRepository.deleteById(id);
    }

    public void approve(Long id, Long adminUserId, String note) {
        Hotel h = hotelRepository.findById(id).orElseThrow();

        // 승인 처리 유효성 검사
        if (h.getApprovalStatus() == Hotel.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("이미 승인된 호텔입니다.");
        }

        h.setApprovalStatus(Hotel.ApprovalStatus.APPROVED);
        h.setApprovalDate(java.time.LocalDateTime.now());
        h.setApprovedBy(adminUserId); // null 허용 (시스템 승인 가능)
        h.setRejectionReason(null);
        hotelRepository.save(h);
    }

    public void reject(Long id, String reason) {
        Hotel h = hotelRepository.findById(id).orElseThrow();
        h.setApprovalStatus(Hotel.ApprovalStatus.REJECTED);
        h.setRejectionReason(reason);
        hotelRepository.save(h);
    }

    public void suspend(Long id, String reason) {
        Hotel h = hotelRepository.findById(id).orElseThrow();
        h.setApprovalStatus(Hotel.ApprovalStatus.SUSPENDED);
        h.setRejectionReason(reason);
        hotelRepository.save(h);
    }
}
