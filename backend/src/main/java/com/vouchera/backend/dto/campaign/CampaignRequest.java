package com.vouchera.backend.dto.campaign;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CampaignRequest {

	@NotNull(message = "companyId is required")
	private UUID companyId;

	@NotBlank(message = "Campaign name is required")
	@Size(max = 255, message = "Campaign name must be at most 255 characters")
	private String name;

	@NotBlank(message = "Campaign description is required")
	@Size(max = 2000, message = "Campaign description must be at most 2000 characters")
	private String description;

	@NotNull(message = "Campaign startTime is required")
	private LocalDateTime startTime;

	@NotNull(message = "Campaign endTime is required")
	private LocalDateTime endTime;


	public UUID getCompanyId() {
		return companyId;
	}

	public void setCompanyId(UUID companyId) {
		this.companyId = companyId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@AssertTrue(message = "Campaign endTime must be after startTime")
	public boolean isValidTimeRange() {
		if (startTime == null || endTime == null) {
			return true;
		}
		return endTime.isAfter(startTime);
	}
}
