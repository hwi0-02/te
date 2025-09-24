package com.example.backend.fe_hotel_detail.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Amenity")
@Getter
@Setter
@NoArgsConstructor
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    public enum FeeType { FREE, PAID, HOURLY }

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType = FeeType.FREE;

    @Column(name = "fee_amount")
    private Integer feeAmount;

    @Column(name = "fee_unit")
    private String feeUnit;

    @Column(name = "operating_hours")
    private String operatingHours;

    private String location;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public enum Category { IN_ROOM, IN_HOTEL, LEISURE, FNB, BUSINESS, OTHER }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category = Category.OTHER;
}
