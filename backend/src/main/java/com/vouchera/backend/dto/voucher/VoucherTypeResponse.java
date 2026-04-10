package com.vouchera.backend.dto.voucher;

import java.util.UUID;

import com.vouchera.backend.entity.VoucherType;

public class VoucherTypeResponse {

    private UUID id;
    private Integer discountPercent;
    private Integer totalQuota;
    private Integer remainingQuota;
    private UUID campaignId;

    public VoucherTypeResponse(
        UUID id,
        Integer discountPercent,
        Integer totalQuota,
        Integer remainingQuota,
        UUID campaignId
    ) {
        this.id = id;
        this.discountPercent = discountPercent;
        this.totalQuota = totalQuota;
        this.remainingQuota = remainingQuota;
        this.campaignId = campaignId;
    }

    public UUID getId() {
        return id;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public Integer getTotalQuota() {
        return totalQuota;
    }

    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public static VoucherTypeResponse fromEntity(VoucherType voucherType) {
        if (voucherType == null) {
            return null;
        }

        return new VoucherTypeResponse(
            voucherType.getId(),
            voucherType.getDiscountPercent(),
            voucherType.getTotalQuota(),
            voucherType.getRemainingQuota(),
            voucherType.getCampaign() == null ? null : voucherType.getCampaign().getId()
        );
    }
}