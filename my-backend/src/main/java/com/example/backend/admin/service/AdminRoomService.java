package com.example.backend.admin.service;

import com.example.backend.fe_hotel_detail.domain.Room;
import com.example.backend.fe_hotel_detail.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminRoomService {
    private final RoomRepository roomRepository;

    public Page<Room> list(Long hotelId, String name, Pageable pageable) {
        if (hotelId != null && name != null) {
            return roomRepository.findByHotelIdAndNameContaining(hotelId, name, pageable);
        } else if (hotelId != null) {
            return roomRepository.findByHotelId(hotelId, pageable);
        } else if (name != null) {
            return roomRepository.findByNameContaining(name, pageable);
        }
        return roomRepository.findAll(pageable);
    }

    public Room get(Long id) { return roomRepository.findById(id).orElseThrow(); }
    public void delete(Long id) { roomRepository.deleteById(id); }
}
