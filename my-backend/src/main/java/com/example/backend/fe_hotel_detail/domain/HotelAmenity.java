package com.example.backend.fe_hotel_detail.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Hotel_Amenity")
@Getter
@Setter
@NoArgsConstructor
public class HotelAmenity {
    @EmbeddedId
    private HotelAmenityId id;

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    public static class HotelAmenityId implements java.io.Serializable {
        @Column(name = "hotel_id")
        private Long hotelId;
        @Column(name = "amenity_id")
        private Long amenityId;
    }
}
