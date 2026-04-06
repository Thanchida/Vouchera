package com.vouchera.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.VoucherType;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.CampaignRepository;
import com.vouchera.backend.repository.VoucherTypeRepository;

@Service
@Transactional
public class VoucherService {

	private final VoucherTypeRepository voucherTypeRepository;
	private final CampaignRepository campaignRepository;

	public VoucherService(VoucherTypeRepository voucherTypeRepository,
						  CampaignRepository campaignRepository) {
		this.voucherTypeRepository = voucherTypeRepository;
		this.campaignRepository = campaignRepository;
	}

	public VoucherType createVoucherType(UUID campaignId, Integer discountPercent, Integer totalQuota) {
		Campaign campaign = campaignRepository.findById(campaignId)
			.orElseThrow(() -> new NotFoundException("Campaign not found"));

		VoucherType voucherType = new VoucherType(campaign, discountPercent, totalQuota);
		return voucherTypeRepository.save(voucherType);
	}

	@Transactional(readOnly = true)
	public VoucherType getVoucherTypeById(UUID voucherTypeId) {
		return voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found"));
	}

	@Transactional(readOnly = true)
	public List<VoucherType> listByCampaign(UUID campaignId) {
		return voucherTypeRepository.findByCampaignId(campaignId);
	}

	@Transactional(readOnly = true)
	public List<VoucherType> listAvailableByCampaign(UUID campaignId) {
		return voucherTypeRepository.findByCampaignIdAndRemainingQuotaGreaterThan(campaignId, 0);
	}

	public VoucherType increaseQuota(UUID voucherTypeId, Integer amount) {
		if (amount == null || amount <= 0) {
			throw new BadRequestException("Increase amount must be greater than 0");
		}

		VoucherType voucherType = voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found"));

		voucherType.setTotalQuota(voucherType.getTotalQuota() + amount);
		voucherType.setRemainingQuota(voucherType.getRemainingQuota() + amount);

		return voucherTypeRepository.save(voucherType);
	}
}
