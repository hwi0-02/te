package com.example.backend.admin.service;

import com.example.backend.admin.domain.Review;
import com.example.backend.admin.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReviewService {
    private final ReviewRepository reviewRepository;

    public Page<Review> list(Pageable pageable) { return reviewRepository.findAll(pageable); }
    public Review get(Long id) { return reviewRepository.findById(id).orElseThrow(); }
    public void delete(Long id) { reviewRepository.deleteById(id); }
}
