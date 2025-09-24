package com.example.backend.admin.controller;

import com.example.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
	@GetMapping("/ping")
	public ResponseEntity<ApiResponse<String>> ping() {
		return ResponseEntity.ok(ApiResponse.ok("admin-api-ok"));
	}
}