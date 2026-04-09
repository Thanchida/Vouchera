package com.vouchera.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vouchera.backend.dto.CurrentUserInfo;
import com.vouchera.backend.dto.campaign.CampaignResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.enums.CampaignStatus;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.CampaignRepository;
import com.vouchera.backend.repository.CompanyRepository;

@Service
@Transactional
public class CampaignService {

	private final CampaignRepository campaignRepository;
	private final CompanyRepository companyRepository;

	public CampaignService(CampaignRepository campaignRepository, 
		CompanyRepository companyRepository) {
		this.campaignRepository = campaignRepository;
		this.companyRepository = companyRepository;
	}

	public CampaignResponse createCampaign(CurrentUserInfo currentUser, UUID companyId, 
		String name,
		String description,
		LocalDateTime startTime,
		LocalDateTime endTime) {
		validateCreateWindow(startTime, LocalDateTime.now());

		validateCampaignCreator(currentUser, companyId);

		Company company = companyRepository.findById(companyId)
			.orElseThrow(() -> new NotFoundException("Company not found"));

		if (company.getCompanyStatus() != CompanyStatus.ACTIVE) {
			throw new IllegalStateException("Cannot create a campaign for a non-ACTIVE company");
		}

		Campaign campaign = new Campaign(
			name.trim(), description.trim(), company, 
			startTime, endTime, CampaignStatus.ACTIVE
		);

		return CampaignResponse.fromEntity(campaignRepository.save(campaign));
	}

	@Transactional(readOnly = true)
	public Page<CampaignResponse> listCampaigns(Pageable pageable) {
		return campaignRepository.findAll(pageable).map(CampaignResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public CampaignResponse getCampaignById(UUID campaignId) {
		return CampaignResponse.fromEntity(getCampaignEntityById(campaignId));
	}

	private Campaign getCampaignEntityById(UUID campaignId) {
		return campaignRepository.findById(campaignId)
			.orElseThrow(() -> new NotFoundException("Campaign not found"));
	}

	@Transactional(readOnly = true)
	public Page<CampaignResponse> listActiveCampaigns(LocalDateTime now, Pageable pageable) {
		LocalDateTime current = requireNow(now);
		return campaignRepository
			.findByStatusAndStartTimeBeforeAndEndTimeAfter(CampaignStatus.ACTIVE, current, current, pageable)
			.map(CampaignResponse::fromEntity);
	}

	@Transactional(readOnly = true)
	public Page<CampaignResponse> listCompanyCampaigns(UUID companyId, CampaignStatus status, Pageable pageable) {
		Page<Campaign> campaigns;
		if (status == null) {
			campaigns = campaignRepository.findByCompanyId(companyId, pageable);
		} else {
			campaigns = campaignRepository.findByCompanyIdAndStatus(companyId, status, pageable);
		}
		return campaigns.map(CampaignResponse::fromEntity);
	}

	public CampaignResponse pauseCampaign(UUID campaignId, CurrentUserInfo currentUser) {
		Campaign campaign = getManagedCampaign(campaignId, currentUser);
		campaign.pause();
		return CampaignResponse.fromEntity(campaignRepository.save(campaign));
	}

	public CampaignResponse resumeCampaign(UUID campaignId, CurrentUserInfo currentUser, LocalDateTime now) {
		Campaign campaign = getManagedCampaign(campaignId, currentUser);
		campaign.resume(requireNow(now));
		return CampaignResponse.fromEntity(campaignRepository.save(campaign));
	}

	public CampaignResponse endCampaign(UUID campaignId, CurrentUserInfo currentUser) {
		Campaign campaign = getManagedCampaign(campaignId, currentUser);
		campaign.end();
		return CampaignResponse.fromEntity(campaignRepository.save(campaign));
	}

	public CampaignResponse updateCampaign(UUID campaignId, 
		CurrentUserInfo currentUser,
		String name,
		String description,
		LocalDateTime startTime,
		LocalDateTime endTime) {
		Campaign campaign = getManagedCampaign(campaignId, currentUser);
		campaign.updateDetails(name, description, startTime, endTime);
		return CampaignResponse.fromEntity(campaignRepository.save(campaign));
	}

	public void deleteCampaign(UUID campaignId, CurrentUserInfo currentUser) {
		Campaign campaign = getManagedCampaign(campaignId, currentUser);
		campaignRepository.delete(campaign);
	}

	private LocalDateTime requireNow(LocalDateTime now) {
		if (now == null) {
			throw new BadRequestException("now cannot be null");
		}
		return now;
	}

	private Campaign getManagedCampaign(UUID campaignId, CurrentUserInfo currentUser) {
		Campaign campaign = getCampaignEntityById(campaignId);

		validateCampaignManager(currentUser, campaign);
		return campaign;
	}

	private void validateCampaignManager(CurrentUserInfo actor, Campaign campaign) {
		if (actor.accountStatus() != com.vouchera.backend.enums.AccountStatus.ACTIVE) {
			throw new ForbiddenException("Account is suspended");
		}
		if (!canManageCampaign(actor, campaign)) {
			throw new ForbiddenException("User is not allowed to manage this campaign");
		}
	}

	private void validateCampaignCreator(CurrentUserInfo actor, UUID companyId) {
		if (actor.accountStatus() != com.vouchera.backend.enums.AccountStatus.ACTIVE) {
			throw new ForbiddenException("Account is suspended");
		}
		if (actor.isAdmin()) {
			return;
		}
		if (!actor.isMarketing()) {
			throw new ForbiddenException("Only ADMIN or MARKETING can create campaigns");
		}
		if (actor.companyId() == null || !actor.companyId().equals(companyId)) {
			throw new ForbiddenException("MARKETING user can only create campaigns for their own company");
		}
	}

	private boolean canManageCampaign(CurrentUserInfo actor, Campaign campaign) {
		if (actor.isAdmin()) {
			return true;
		}
		return actor.isMarketing()
			&& actor.companyId() != null
			&& campaign.getCompany() != null
			&& actor.companyId().equals(campaign.getCompany().getId());
	}

	private void validateCreateWindow(LocalDateTime startTime, LocalDateTime now) {
		if (startTime.isBefore(now)) {
			throw new BadRequestException("Campaign startTime cannot be in the past");
		}
	}

}
