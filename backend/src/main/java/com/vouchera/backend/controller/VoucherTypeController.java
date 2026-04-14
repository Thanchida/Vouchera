package com.vouchera.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vouchera.backend.dto.voucher.VoucherTypeResponse;
import com.vouchera.backend.service.VoucherTypeService;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@Validated
@RequestMapping("/api/voucher-types")
public class VoucherTypeController {

    private final VoucherTypeService voucherService;

    public VoucherTypeController(VoucherTypeService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping("/campaigns/{campaignId}")
    public ResponseEntity<VoucherTypeResponse> createVoucherType(
        @PathVariable UUID campaignId,
        @RequestParam @NotNull @Positive Integer discountPercent,
        @RequestParam @NotNull @Positive Integer totalQuota
    ) {
        return ResponseEntity.ok(voucherService.createVoucherType(campaignId, discountPercent, totalQuota));
    }

    @PostMapping("/{voucherTypeId}/increase-quota")
    public ResponseEntity<VoucherTypeResponse> increaseQuota(
        @PathVariable UUID voucherTypeId,
        @RequestParam @NotNull @Positive Integer amount
    ) {
        return ResponseEntity.ok(voucherService.increaseQuota(voucherTypeId, amount));
    }

    @GetMapping("/{voucherTypeId}")
    public ResponseEntity<VoucherTypeResponse> getVoucherType(@PathVariable UUID voucherTypeId) {
        return ResponseEntity.ok(voucherService.getVoucherTypeById(voucherTypeId));
    }

    @GetMapping("/campaigns/{campaignId}")
    public List<VoucherTypeResponse> listByCampaign(@PathVariable UUID campaignId) {
        return voucherService.listByCampaign(campaignId);
    }

    @GetMapping("/campaigns/{campaignId}/available")
    public List<VoucherTypeResponse> listAvailableByCampaign(@PathVariable UUID campaignId) {
        return voucherService.listAvailableByCampaign(campaignId);
    }
}