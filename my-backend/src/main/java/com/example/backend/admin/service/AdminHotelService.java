package com.example.backend.admin.service;

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

    public Page<Hotel> list(String name, Integer minStar, Hotel.ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return hotelRepository.searchByApproval(status.name(), name, minStar, pageable);
        }
        if (name != null || minStar != null) {
            return hotelRepository.search(name, minStar, pageable);
        }
        return hotelRepository.findAll(pageable);
    }

    public Hotel get(Long id) {
        return hotelRepository.findById(id).orElseThrow();
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
