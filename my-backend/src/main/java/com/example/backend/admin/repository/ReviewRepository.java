package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	@Query("SELECT r FROM Review r")
	Page<Review> search(Pageable pageable);
}
