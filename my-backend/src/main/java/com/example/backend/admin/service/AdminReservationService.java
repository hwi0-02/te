package com.example.backend.admin.service;

import com.example.backend.admin.domain.Reservation;
import com.example.backend.admin.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminReservationService {
    private final ReservationRepository reservationRepository;

    public Page<Reservation> list(Reservation.Status status, LocalDateTime from, LocalDateTime to,
                                  LocalDateTime stayFrom, LocalDateTime stayTo,
                                  Pageable pageable) {
        return reservationRepository.search(status, from, to, stayFrom, stayTo, pageable);
    }

    public Reservation get(Long id) { return reservationRepository.findById(id).orElseThrow(); }

    public void cancel(Long id) {
        Reservation r = reservationRepository.findById(id).orElseThrow();
        r.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(r);
    }
}
