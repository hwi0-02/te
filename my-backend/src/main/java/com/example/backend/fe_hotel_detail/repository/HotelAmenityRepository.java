package com.example.backend.fe_hotel_detail.repository;

import com.example.backend.fe_hotel_detail.domain.HotelAmenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, HotelAmenity.HotelAmenityId> {}
