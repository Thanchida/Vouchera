package com.vouchera.backend.dto.redemption;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vouchera.backend.dto.voucher.VoucherTypeResponse;
import com.vouchera.backend.enums.RedemptionStatus;

public class RedemptionResponse {

    private UUID id;
    private UUID userId;
    private VoucherTypeResponse voucherType;
    private LocalDateTime redeemedAt;
    private LocalDateTime usedAt;
    private RedemptionStatus status;

    public RedemptionResponse(
        UUID id,
        UUID userId,
        VoucherTypeResponse voucherType,
        LocalDateTime redeemedAt,
        LocalDateTime usedAt,
        RedemptionStatus status
    ) {
        this.id = id;
        this.userId = userId;
        this.voucherType = voucherType;
        this.redeemedAt = redeemedAt;
        this.usedAt = usedAt;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public VoucherTypeResponse getVoucherType() {
        return voucherType;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public RedemptionStatus getStatus() {
        return status;
    }
}