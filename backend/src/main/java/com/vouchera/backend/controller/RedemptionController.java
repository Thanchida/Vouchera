package com.vouchera.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vouchera.backend.dto.auth.CurrentUserInfo;
import com.vouchera.backend.dto.redemption.RedemptionRequest;
import com.vouchera.backend.dto.redemption.RedemptionResponse;
import com.vouchera.backend.enums.RedemptionStatus;
import com.vouchera.backend.service.AuthService;
import com.vouchera.backend.service.RedemptionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/redemptions")
public class RedemptionController {

	private final RedemptionService redemptionService;
	private final AuthService authService;

	public RedemptionController(RedemptionService redemptionService, AuthService authService) {
		this.redemptionService = redemptionService;
		this.authService = authService;
	}

	@PostMapping("/redeem")
	public ResponseEntity<RedemptionResponse> redeem(@Valid @RequestBody RedemptionRequest request) {
		CurrentUserInfo currentUser = authService.getCurrentUserInfo();
		return ResponseEntity.ok(redemptionService.redeemCoupon(currentUser, request.getVoucherTypeId()));
	}

	@GetMapping("/users/{userId}")
	public List<RedemptionResponse> getUserRedemptions(@PathVariable UUID userId) {
		CurrentUserInfo currentUser = authService.getCurrentUserInfo();
		return redemptionService.getUserRedemptions(userId, currentUser);
	}

	@GetMapping("/users/{userId}/status/{status}")
	public List<RedemptionResponse> getUserRedemptionsByStatus(
		@PathVariable UUID userId,
		@PathVariable RedemptionStatus status
	) {
		CurrentUserInfo currentUser = authService.getCurrentUserInfo();
		return redemptionService.getUserRedemptionsByStatus(userId, currentUser, status);
	}

	@PostMapping("/{redemptionId}/use")
	public ResponseEntity<RedemptionResponse> markUsed(
		@PathVariable UUID redemptionId
	) {
		CurrentUserInfo currentUser = authService.getCurrentUserInfo();
		return ResponseEntity.ok(redemptionService.markRedemptionUsed(redemptionId, currentUser));
	}

	@PostMapping("/{redemptionId}/expire")
	public ResponseEntity<RedemptionResponse> expire(@PathVariable UUID redemptionId) {
		CurrentUserInfo currentUser = authService.getCurrentUserInfo();
		return ResponseEntity.ok(redemptionService.expireRedemption(redemptionId, currentUser));
	}
}
