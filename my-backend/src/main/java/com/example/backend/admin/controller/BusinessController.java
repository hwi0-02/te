package com.example.backend.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {
	@GetMapping("/ping")
	public ResponseEntity<String> ping() {
		return ResponseEntity.ok("business-api-ok");
	}
}