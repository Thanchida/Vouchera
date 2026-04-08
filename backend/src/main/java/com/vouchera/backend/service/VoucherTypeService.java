package com.vouchera.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.dto.voucher.VoucherTypeResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.VoucherType;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.mapper.VoucherTypeMapper;
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
		return VoucherTypeMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public VoucherTypeResponse getVoucherTypeById(UUID voucherTypeId) {
		return VoucherTypeMapper.toResponse(voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found")));
	}

	@Transactional(readOnly = true)
	public List<VoucherTypeResponse> listByCampaign(UUID campaignId) {
		return VoucherTypeMapper.toResponseList(voucherTypeRepository
			.findByCampaignId(campaignId));
	}

	@Transactional(readOnly = true)
	public List<VoucherTypeResponse> listAvailableByCampaign(UUID campaignId) {
		return VoucherTypeMapper.toResponseList(voucherTypeRepository
			.findByCampaignIdAndRemainingQuotaGreaterThan(campaignId, 0));
	}

	public VoucherTypeResponse increaseQuota(UUID voucherTypeId, Integer amount) {
		VoucherType voucherType = voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found"));

		voucherType.increaseQuota(amount);
		VoucherType saved = voucherTypeRepository.save(voucherType);

		return VoucherTypeMapper.toResponse(saved);
	}
}
