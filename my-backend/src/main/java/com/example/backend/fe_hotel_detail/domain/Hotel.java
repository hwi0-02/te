package com.example.backend.fe_hotel_detail.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.backend.fe_hotel_detail.domain.Hotel;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "hotel")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable=false, length=100)
    private String name;

    @Column(nullable=false, length=255)
    private String address;

    @Column(name = "star_rating")
    @Builder.Default
    private Integer starRating = 0; // 기본값 0으로 설정

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Lob
    private String description;

    @Column(length=50)
    private String country;

    public enum ApprovalStatus { PENDING, APPROVED, REJECTED, SUSPENDED }

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "approval_date")
    private java.time.LocalDateTime approvalDate;

    @Column(name = "approved_by")
    private Long approvedBy; // null 허용 (시스템 승인 가능)

    @Lob
    @Column(name = "rejection_reason")
    private String rejectionReason;

    // Lombok @Builder will generate builder() method automatically.
    
    @PrePersist
    protected void onCreate() {
        if (starRating == null) {
            starRating = 0;
        }
        if (approvalStatus == null) {
            approvalStatus = ApprovalStatus.PENDING;
        }
    }

}
