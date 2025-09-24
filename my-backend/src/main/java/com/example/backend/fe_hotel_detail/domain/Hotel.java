package com.example.backend.fe_hotel_detail.domain;

import com.example.backend.fe_hotel_detail.domain.Hotel;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "Hotel")
@Getter @Setter @NoArgsConstructor
public class Hotel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100)
    private String name;

    @Column(nullable=false, length=255)
    private String address;

    @Column(name = "star_rating")
    private Integer starRating;

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
    private Long approvedBy;

    @Lob
    @Column(name = "rejection_reason")
    private String rejectionReason;

}
