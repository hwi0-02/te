package com.example.backend.fe_hotel_detail.repository;

import com.example.backend.fe_hotel_detail.domain.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {}
