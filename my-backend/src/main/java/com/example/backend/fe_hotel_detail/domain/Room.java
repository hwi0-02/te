package com.example.backend.fe_hotel_detail.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "Room")
@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "room_size", nullable = false, length = 50)
    private String roomSize;

    @Column(name = "capacity_min", nullable = false)
    private Integer capacityMin;

    @Column(name = "capacity_max", nullable = false)
    private Integer capacityMax;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @Column(name = "view_name", length = 50)
    private String viewName;

    @Column(length = 50)
    private String bed;

    private Integer bath;
    private Boolean smoke;
    private Boolean sharedBath;
    private Boolean hasWindow;
    private Boolean aircon;
    private Boolean freeWater;
    private Boolean wifi;

    @Column(name = "cancel_policy", length = 100)
    private String cancelPolicy;

    @Column(length = 50)
    private String payment;

    private Integer originalPrice;
    private Integer price;
}
