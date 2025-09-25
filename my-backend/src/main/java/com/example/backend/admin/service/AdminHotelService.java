package com.example.backend.admin.service;

import com.example.backend.dto.HotelAdminDto;
import com.example.backend.fe_hotel_detail.domain.Hotel;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminHotelService {
    private final HotelRepository hotelRepository;

    public Page<HotelAdminDto> list(String name, Integer minStar, Hotel.ApprovalStatus status, Pageable pageable) {
        Page<Hotel> hotels;
        if (status != null) {
            hotels = hotelRepository.searchByApproval(status.name(), name, minStar, pageable);
        } else if (name != null || minStar != null) {
            hotels = hotelRepository.search(name, minStar, pageable);
        } else {
            hotels = hotelRepository.findAll(pageable);
        }
        return hotels.map(HotelAdminDto::from);
    }

    public HotelAdminDto get(Long id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow();
        return HotelAdminDto.from(hotel);
    }

    public void delete(Long id) {
        hotelRepository.deleteById(id);
    }

    public void approve(Long id, Long adminUserId, String note) {
        Hotel h = hotelRepository.findById(id).orElseThrow();
        h.setApprovalStatus(Hotel.ApprovalStatus.APPROVED);
        h.setApprovalDate(java.time.LocalDateTime.now());
        h.setApprovedBy(adminUserId);
        h.setRejectionReason(null);
        hotelRepository.save(h);
    }

    public void reject(Long id, String reason) {
        Hotel h = hotelRepository.findById(id).orElseThrow();
        h.setApprovalStatus(Hotel.ApprovalStatus.REJECTED);
        h.setRejectionReason(reason);
        hotelRepository.save(h);
    }

    public void suspend(Long id, String reason) {
        Hotel h = hotelRepository.findById(id).orElseThrow();
        h.setApprovalStatus(Hotel.ApprovalStatus.SUSPENDED);
        h.setRejectionReason(reason);
        hotelRepository.save(h);
    }
}
