package com.vouchera.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.dto.voucher.VoucherTypeResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.VoucherType;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.CampaignRepository;
import com.vouchera.backend.repository.VoucherTypeRepository;

@Service
@Transactional
public class VoucherTypeService {

	private final VoucherTypeRepository voucherTypeRepository;
	private final CampaignRepository campaignRepository;

	public VoucherTypeService(VoucherTypeRepository voucherTypeRepository,
						  CampaignRepository campaignRepository) {
		this.voucherTypeRepository = voucherTypeRepository;
		this.campaignRepository = campaignRepository;
	}

	public VoucherTypeResponse createVoucherType(UUID campaignId, Integer discountPercent, Integer totalQuota) {
		Campaign campaign = campaignRepository.findById(campaignId)
			.orElseThrow(() -> new NotFoundException("Campaign not found"));

		VoucherType voucherType = new VoucherType(campaign, discountPercent, totalQuota);
		VoucherType saved = voucherTypeRepository.save(voucherType);
		return VoucherTypeResponse.fromEntity(saved);
	}

	@Transactional(readOnly = true)
	public VoucherTypeResponse getVoucherTypeById(UUID voucherTypeId) {
		return VoucherTypeResponse.fromEntity(voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found")));
	}

	@Transactional(readOnly = true)
	public List<VoucherTypeResponse> listByCampaign(UUID campaignId) {
		return voucherTypeRepository
			.findByCampaignId(campaignId)
			.stream()
			.map(VoucherTypeResponse::fromEntity)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<VoucherTypeResponse> listAvailableByCampaign(UUID campaignId) {
		return voucherTypeRepository
			.findByCampaignIdAndRemainingQuotaGreaterThan(campaignId, 0)
			.stream()
			.map(VoucherTypeResponse::fromEntity)
			.toList();
	}

	public VoucherTypeResponse increaseQuota(UUID voucherTypeId, Integer amount) {
		VoucherType voucherType = voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found"));

		voucherType.increaseQuota(amount);
		VoucherType saved = voucherTypeRepository.save(voucherType);

		return VoucherTypeResponse.fromEntity(saved);
	}
}
