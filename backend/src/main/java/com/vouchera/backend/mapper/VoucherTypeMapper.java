package com.vouchera.backend.mapper;

import java.util.List;

import com.vouchera.backend.dto.voucher.VoucherTypeResponse;
import com.vouchera.backend.entity.VoucherType;

public final class VoucherTypeMapper {

    private VoucherTypeMapper() {
    }

    public static VoucherTypeResponse toResponse(VoucherType voucherType) {
        return new VoucherTypeResponse(
            voucherType.getId(),
            voucherType.getDiscountPercent(),
            voucherType.getTotalQuota(),
            voucherType.getRemainingQuota(),
            voucherType.getCampaign() == null ? null : voucherType.getCampaign().getId()
        );
    }

    public static List<VoucherTypeResponse> toResponseList(List<VoucherType> voucherTypes) {
        return voucherTypes.stream().map(VoucherTypeMapper::toResponse).toList();
    }
}