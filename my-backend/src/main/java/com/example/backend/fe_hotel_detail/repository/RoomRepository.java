package com.example.backend.fe_hotel_detail.repository;

import com.example.backend.fe_hotel_detail.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    java.util.List<Room> findByHotelId(Long hotelId);
    Page<Room> findByHotelId(Long hotelId, Pageable pageable);
    Page<Room> findByNameContaining(String name, Pageable pageable);
    Page<Room> findByHotelIdAndNameContaining(Long hotelId, String name, Pageable pageable);
}
