package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Review;
import com.example.backend.admin.service.AdminReviewService;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminReviewController {

    private final AdminReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Review>>> list(Pageable pageable) {
        var page = reviewService.list(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.get(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
