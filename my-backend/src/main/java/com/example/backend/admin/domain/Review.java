package com.example.backend.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "wrote_on", nullable = false)
    private LocalDateTime wroteOn;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;

    @Lob
    private String content;

    @Lob
    private String image;
}
