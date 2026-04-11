package com.vouchera.backend.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vouchera.backend.dto.campaign.CampaignRequest;
import com.vouchera.backend.dto.campaign.CampaignResponse;
import com.vouchera.backend.dto.campaign.UpdateCampaignRequest;
import com.vouchera.backend.enums.CampaignStatus;
import com.vouchera.backend.service.AuthService;
import com.vouchera.backend.service.CampaignService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class CampaignController {

	private final CampaignService campaignService;
	private final AuthService authService;

	public CampaignController(CampaignService campaignService, AuthService authService) {
		this.campaignService = campaignService;
		this.authService = authService;
	}

	@GetMapping("/campaigns")
	public Page<CampaignResponse> listCampaigns(Pageable pageable) {
		return campaignService.listCampaigns(pageable);
	}

	@GetMapping("/campaigns/{campaignId}")
	public ResponseEntity<CampaignResponse> getCampaign(@PathVariable UUID campaignId) {
		return ResponseEntity.ok(campaignService.getCampaignById(campaignId));
	}

	@GetMapping("/campaigns/active")
	public Page<CampaignResponse> listActiveCampaigns(
		@RequestParam(required = false) LocalDateTime now,
		Pageable pageable
	) {
		LocalDateTime current = now == null ? LocalDateTime.now() : now;
		return campaignService.listActiveCampaigns(current, pageable);
	}

	@GetMapping("/companies/{companyId}/campaigns")
	public Page<CampaignResponse> listCompanyCampaigns(
		@PathVariable UUID companyId,
		@RequestParam(required = false) CampaignStatus status,
		Pageable pageable
	) {
		return campaignService.listCompanyCampaigns(companyId, status, pageable);
	}

	@PostMapping("/campaigns")
	public ResponseEntity<CampaignResponse> createCampaign(@Valid @RequestBody CampaignRequest request) {
		var currentUser = authService.getCurrentUserInfo();
		CampaignResponse created = campaignService.createCampaign(
			currentUser,
			request.getCompanyId(),
			request.getName(),
			request.getDescription(),
			request.getStartTime(),
			request.getEndTime()
		);
		return ResponseEntity.ok(created);
	}

	@PutMapping("/campaigns/{campaignId}")
	public ResponseEntity<CampaignResponse> updateCampaign(
		@PathVariable UUID campaignId,
		@Valid @RequestBody UpdateCampaignRequest request
	) {
		var currentUser = authService.getCurrentUserInfo();
		CampaignResponse updated = campaignService.updateCampaign(
			campaignId,
			currentUser,
			request.getName(),
			request.getDescription(),
			request.getStartTime(),
			request.getEndTime()
		);
		return ResponseEntity.ok(updated);
	}

	@PostMapping("/campaigns/{campaignId}/pause")
	public ResponseEntity<CampaignResponse> pauseCampaign(
		@PathVariable UUID campaignId
	) {
		var currentUser = authService.getCurrentUserInfo();
		return ResponseEntity.ok(campaignService.pauseCampaign(campaignId, currentUser));
	}

	@PostMapping("/campaigns/{campaignId}/resume")
	public ResponseEntity<CampaignResponse> resumeCampaign(
		@PathVariable UUID campaignId,
		@RequestParam(required = false) LocalDateTime now
	) {
		var currentUser = authService.getCurrentUserInfo();
		LocalDateTime current = now == null ? LocalDateTime.now() : now;
		return ResponseEntity.ok(campaignService.resumeCampaign(campaignId, currentUser, current));
	}

	@PostMapping("/campaigns/{campaignId}/end")
	public ResponseEntity<CampaignResponse> endCampaign(
		@PathVariable UUID campaignId
	) {
		var currentUser = authService.getCurrentUserInfo();
		return ResponseEntity.ok(campaignService.endCampaign(campaignId, currentUser));
	}

	@DeleteMapping("/campaigns/{campaignId}")
	public ResponseEntity<Void> deleteCampaign(
		@PathVariable UUID campaignId
	) {
		var currentUser = authService.getCurrentUserInfo();
		campaignService.deleteCampaign(campaignId, currentUser);
		return ResponseEntity.noContent().build();
	}
}
