package com.example.backend.admin.service;

import com.example.backend.admin.domain.Reservation;
import com.example.backend.admin.repository.PaymentRepository;
import com.example.backend.admin.repository.ReservationRepository;
import com.example.backend.admin.dto.ReservationDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReservationService {
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public Page<Reservation> list(Reservation.Status status, LocalDateTime from, LocalDateTime to,
                                  LocalDateTime stayFrom, LocalDateTime stayTo,
                                  Pageable pageable) {
        return reservationRepository.search(status, from, to, stayFrom, stayTo, pageable);
    }
    
    // 상세 정보 포함 예약 목록 조회
    public Page<ReservationDetailDto> listWithDetails(Reservation.Status reservationStatus, 
                                                     LocalDateTime from, LocalDateTime to,
                                                     LocalDateTime stayFrom, LocalDateTime stayTo,
                                                     String hotelName, String userName, 
                                                     String paymentStatus,
                                                     Pageable pageable) {
        String statusStr = reservationStatus != null ? reservationStatus.name() : null;
        
        // 디버깅: 전체 결제 데이터 수 확인
        Long totalPayments = paymentRepository.countAllPayments();
        log.info("=== 결제 데이터 디버깅 ===");
        log.info("전체 결제 데이터 수: {}", totalPayments);
        
        Page<Object[]> results = reservationRepository.searchWithDetails(
            statusStr, from, to, stayFrom, stayTo, hotelName, userName, paymentStatus, pageable);
        
        log.info("조회된 예약 결과 수: {}", results.getContent().size());
        
        List<ReservationDetailDto> dtos = results.getContent().stream()
            .map(this::mapArrayToDto)
            .toList();
            
        return new PageImpl<>(dtos, pageable, results.getTotalElements());
    }

    public Reservation get(Long id) { 
        return reservationRepository.findById(id).orElseThrow(); 
    }
    
    // 상세 정보 포함 단일 예약 조회
    public ReservationDetailDto getDetail(Long id) {
        log.info("예약 상세 조회 시작 - ID: {}", id);
        
        try {
            if (id == null || id <= 0) {
                log.warn("잘못된 예약 ID: {}", id);
                throw new IllegalArgumentException("유효하지 않은 예약 ID입니다: " + id);
            }

            if (!reservationRepository.existsById(id)) {
                log.warn("예약이 존재하지 않음 - ID: {}", id);
                throw new RuntimeException("예약을 찾을 수 없습니다: " + id);
            }

            Object[] result = reservationRepository.findDetailById(id);
            if (result == null) {
                log.warn("예약 상세 조회 결과가 null - ID: {}", id);
                throw new RuntimeException("예약 상세 정보를 찾을 수 없습니다: " + id);
            }

            log.info("예약 상세 조회 성공 - ID: {}, 데이터 길이: {}", id, result.length);
            
            // 데이터 무결성 검증
            if (result.length < 9) {
                log.warn("예약 기본 정보가 불완전 - ID: {}, 데이터 길이: {}", id, result.length);
                throw new RuntimeException("예약 기본 정보가 불완전합니다: " + id);
            }
            
            // 예약 ID가 일치하는지 확인
            Long reservationId = safeLong(result[0]);
            if (reservationId == null || !reservationId.equals(id)) {
                log.warn("예약 ID 불일치 - 요청 ID: {}, 조회된 ID: {}", id, reservationId);
                throw new RuntimeException("예약 ID가 일치하지 않습니다: " + id);
            }
            
            return mapArrayToDto(result);
            
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("데이터베이스 접근 오류 - ID: {}, SQL 오류: {}", id, e.getMessage());
            throw new RuntimeException("데이터베이스 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            log.error("예약 상세 데이터 매핑 오류 - ID: {}, 오류: {}", id, e.getMessage());
            throw new RuntimeException("예약 데이터 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("예약 상세 조회 중 예상치 못한 오류 - ID: {}, 오류 타입: {}", id, e.getClass().getSimpleName(), e);
            throw new RuntimeException("예약 상세 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public void cancel(Long id) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        r.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(r);
    }
    
    private ReservationDetailDto mapArrayToDto(Object[] row) {
        if (row == null) {
            log.warn("데이터 매핑 오류: 조회된 데이터가 null입니다");
            throw new IllegalArgumentException("조회된 데이터가 null입니다");
        }
        
        // 예약 기본 정보는 필수 (0-8), 나머지는 LEFT JOIN으로 인해 NULL 가능
        if (row.length < 9) {
            log.warn("데이터 매핑 오류: 예약 기본 정보가 불완전합니다. 최소: 9개 필드, 실제: {}", row.length);
            throw new IllegalArgumentException("예약 기본 정보가 불완전합니다. 최소: 9개 필드, 실제: " + row.length);
        }

        log.debug("데이터 매핑 시작 - 배열 길이: {}, 예약 ID: {}", row.length, row[0]);
        
        // 각 필드의 값을 안전하게 추출
        try {
            return ReservationDetailDto.builder()
                // 예약 기본 정보 (0-8) - 필수 정보
                .reservationId(safeLong(row[0]))
                .transactionId(safeString(row[1]))
                .numAdult(safeInteger(row[2]))
                .numKid(safeInteger(row[3]))
                .startDate(safeDateTime(row[4]))
                .endDate(safeDateTime(row[5]))
                .createdAt(safeDateTime(row[6]))
                .reservationStatus(safeString(row[7]))
                .expiresAt(safeDateTime(row[8]))

                // 호텔 정보 (9-12) - LEFT JOIN, NULL 허용
                .hotelId(safeLongWithDefault(row, 9))
                .hotelName(safeStringWithDefault(row, 10, "호텔 정보 없음"))
                .hotelAddress(safeStringWithDefault(row, 11, "주소 정보 없음"))
                .starRating(safeIntegerWithDefault(row, 12, 0))

                // 객실 정보 (13-18) - LEFT JOIN, NULL 허용
                .roomId(safeLongWithDefault(row, 13))
                .roomName(safeStringWithDefault(row, 14, "객실 정보 없음"))
                .roomType(safeStringWithDefault(row, 15, "객실 타입 정보 없음"))
                .capacityMin(safeIntegerWithDefault(row, 16, 0))
                .capacityMax(safeIntegerWithDefault(row, 17, 0))
                .roomPrice(safeIntegerWithDefault(row, 18, 0))

                // 사용자 정보 (19-22) - LEFT JOIN, NULL 허용
                .userId(safeLongWithDefault(row, 19))
                .userName(safeStringWithDefault(row, 20, "사용자 정보 없음"))
                .userEmail(safeStringWithDefault(row, 21, "이메일 정보 없음"))
                .userPhone(safeStringWithDefault(row, 22, "전화번호 정보 없음"))

                // 결제 정보 (23-30) - LEFT JOIN, NULL 허용
                .paymentId(safeLongWithDefault(row, 23))
                .paymentMethod(safeStringWithDefault(row, 24, "결제 정보 없음"))
                .basePrice(safeIntegerWithDefault(row, 25, 0))
                .totalPrice(safeIntegerWithDefault(row, 26, 0))
                .tax(safeIntegerWithDefault(row, 27, 0))
                .discount(safeIntegerWithDefault(row, 28, 0))
                .paymentStatus(safeStringWithDefault(row, 29, "결제 정보 없음"))
                .paymentCreatedAt(safeDateTimeWithDefault(row, 30))
                .receiptUrl(safeStringWithDefault(row, 31, null)) // receiptUrl은 31번째 인덱스
                .build();
        } catch (Exception e) {
            log.error("DTO 매핑 중 오류 발생: {}", e.getMessage());
            log.error("데이터 배열: {}", java.util.Arrays.toString(row));
            throw new RuntimeException("데이터 매핑 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private Long safeLong(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) return ((Number) obj).longValue();
            if (obj instanceof String) return Long.parseLong((String) obj);
            return null;
        } catch (Exception e) {
            log.warn("Long 변환 실패: {}", obj, e);
            return null;
        }
    }

    private Integer safeInteger(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) return ((Number) obj).intValue();
            if (obj instanceof String) return Integer.parseInt((String) obj);
            return null;
        } catch (Exception e) {
            log.warn("Integer 변환 실패: {}", obj, e);
            return null;
        }
    }

    private String safeString(Object obj) {
        if (obj == null) return null;
        try {
            return obj.toString().trim();
        } catch (Exception e) {
            log.warn("String 변환 실패: {}", obj, e);
            return null;
        }
    }

    private LocalDateTime safeDateTime(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
            if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
            if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate().atStartOfDay();
            if (obj instanceof java.util.Date) return ((java.util.Date) obj).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            return null;
        } catch (Exception e) {
            log.warn("LocalDateTime 변환 실패: {}", obj, e);
            return null;
        }
    }

    // 기본값을 제공하는 안전한 변환 메서드들
    private Long safeLongWithDefault(Object[] row, int index) {
        if (row.length <= index) return null;
        return safeLong(row[index]);
    }

    private Integer safeIntegerWithDefault(Object[] row, int index, Integer defaultValue) {
        if (row.length <= index) return defaultValue;
        Integer value = safeInteger(row[index]);
        return value != null ? value : defaultValue;
    }

    private String safeStringWithDefault(Object[] row, int index, String defaultValue) {
        if (row.length <= index) return defaultValue;
        String value = safeString(row[index]);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    private LocalDateTime safeDateTimeWithDefault(Object[] row, int index) {
        if (row.length <= index) return null;
        return safeDateTime(row[index]);
    }
}
