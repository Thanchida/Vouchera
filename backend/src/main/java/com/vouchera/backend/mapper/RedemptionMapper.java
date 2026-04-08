package com.vouchera.backend.mapper;

import java.util.List;

import com.vouchera.backend.dto.redemption.RedemptionResponse;
import com.vouchera.backend.entity.Redemption;

public final class RedemptionMapper {

    private RedemptionMapper() {
    }

    public static RedemptionResponse toResponse(Redemption redemption) {
        return new RedemptionResponse(
            redemption.getId(),
            redemption.getUser() == null ? null : redemption.getUser().getId(),
            redemption.getVoucherType() == null ? null : VoucherTypeMapper.toResponse(redemption.getVoucherType()),
            redemption.getRedeemedAt(),
            redemption.getUsedAt(),
            redemption.getStatus()
        );
    }

    public static List<RedemptionResponse> toResponseList(List<Redemption> redemptions) {
        return redemptions.stream().map(RedemptionMapper::toResponse).toList();
    }
}