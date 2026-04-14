package com.vouchera.backend.dto.redemption;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class RedemptionRequest {

	@NotNull(message = "voucherTypeId is required")
	private UUID voucherTypeId;
	private LocalDateTime now;

	public UUID getVoucherTypeId() {
		return voucherTypeId;
	}

	public void setVoucherTypeId(UUID voucherTypeId) {
		this.voucherTypeId = voucherTypeId;
	}

	public LocalDateTime getNow() {
		return now;
	}

	public void setNow(LocalDateTime now) {
		this.now = now;
	}
}
