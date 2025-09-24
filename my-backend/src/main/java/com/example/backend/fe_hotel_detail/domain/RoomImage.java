package com.example.backend.fe_hotel_detail.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_image")
@Getter
@Setter
@NoArgsConstructor
public class RoomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable=false)
    private Long roomId;

    @Column(nullable=false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "sort_no", nullable=false)
    private Integer sortNo = 0;

    @Column(name = "is_cover", nullable=false)
    private Boolean cover = Boolean.FALSE;

    @Column(name = "caption", length=255)
    private String caption;

    @Column(name = "alt_text", length=255)
    private String altText;
}
