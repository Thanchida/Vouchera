package com.vouchera.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vouchera.backend.dto.auth.CurrentUserInfo;
import com.vouchera.backend.dto.campaign.CampaignResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.enums.CampaignStatus;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ConflictException;
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
		LocalDateTime now = LocalDateTime.now();
		validateCreateWindow(startTime, now);

		validateCampaignCreator(currentUser, companyId);

		Company company = companyRepository.findById(companyId)
			.orElseThrow(() -> new NotFoundException("Company not found"));

		if (company.getCompanyStatus() != CompanyStatus.ACTIVE) {
			throw new ConflictException("Cannot create a campaign for a non-ACTIVE company");
		}

		Campaign campaign = new Campaign(
			name.trim(), description.trim(), company, 
			startTime, endTime, resolveInitialStatus(startTime, now)
		);

		return CampaignResponse.fromEntity(campaignRepository.save(campaign));
	}

	public Page<CampaignResponse> listCampaigns(Pageable pageable) {
		Page<Campaign> campaigns = campaignRepository.findAll(pageable);
		syncStatuses(campaigns.getContent(), LocalDateTime.now());
		return campaigns.map(CampaignResponse::fromEntity);
	}

	public CampaignResponse getCampaignById(UUID campaignId) {
		Campaign campaign = getCampaignEntityById(campaignId);
		syncStatus(campaign, LocalDateTime.now());
		return CampaignResponse.fromEntity(campaign);
	}

	private Campaign getCampaignEntityById(UUID campaignId) {
		return campaignRepository.findById(campaignId)
			.orElseThrow(() -> new NotFoundException("Campaign not found"));
	}

	public Page<CampaignResponse> listActiveCampaigns(LocalDateTime now, Pageable pageable) {
		LocalDateTime current = requireNow(now);
		Page<Campaign> campaigns = campaignRepository
			.findByStatusInAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
				List.of(CampaignStatus.PENDING, CampaignStatus.ACTIVE),
				current,
				current,
				pageable
			);
		syncStatuses(campaigns.getContent(), current);
		return campaigns.map(CampaignResponse::fromEntity);
	}

	public Page<CampaignResponse> listCompanyCampaigns(UUID companyId, CampaignStatus status, Pageable pageable) {
		Page<Campaign> campaigns;
		if (status == null) {
			campaigns = campaignRepository.findByCompanyId(companyId, pageable);
		} else {
			campaigns = campaignRepository.findByCompanyIdAndStatus(companyId, status, pageable);
		}
		syncStatuses(campaigns.getContent(), LocalDateTime.now());
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

	public CampaignResponse updateCampaignStatus(UUID campaignId, CurrentUserInfo currentUser, CampaignStatus newStatus) {
		Campaign campaign = getManagedCampaign(campaignId, currentUser);
		LocalDateTime now = LocalDateTime.now();

		CampaignStatus currentStatus = campaign.getStatus();
		
		switch (newStatus) {
			case PENDING:
				throw new ConflictException("Campaign status cannot be manually changed to PENDING. It becomes PENDING automatically based on schedule.");
			case ACTIVE:
				if (currentStatus == CampaignStatus.PENDING) {
					campaign.syncLifecycleStatus(now);
					if (campaign.getStatus() != CampaignStatus.ACTIVE) {
						throw new ConflictException("Campaign cannot be activated before its start time");
					}
				} else if (currentStatus == CampaignStatus.PAUSED) {
					campaign.resume(now);
				} else if (currentStatus != CampaignStatus.ACTIVE) {
					throw new ConflictException("Cannot transition from " + currentStatus + " to ACTIVE");
				}
				break;
			case PAUSED:
				if (currentStatus != CampaignStatus.ACTIVE) {
					throw new ConflictException("Only ACTIVE campaigns can be paused");
				}
				campaign.pause();
				break;
			case ENDED:
				if (currentStatus == CampaignStatus.ENDED) {
					throw new ConflictException("Campaign is already ended");
				}
				campaign.end();
				break;
		}

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

	public int syncAllLifecycleStatuses(LocalDateTime now) {
		LocalDateTime current = requireNow(now);
		List<Campaign> campaigns = campaignRepository.findByStatusIn(
			List.of(CampaignStatus.PENDING, CampaignStatus.ACTIVE, CampaignStatus.PAUSED)
		);
		List<Campaign> changedCampaigns = new ArrayList<>();
		for (Campaign campaign : campaigns) {
			if (campaign.syncLifecycleStatus(current)) {
				changedCampaigns.add(campaign);
			}
		}
		if (!changedCampaigns.isEmpty()) {
			campaignRepository.saveAll(changedCampaigns);
		}
		return changedCampaigns.size();
	}

	private LocalDateTime requireNow(LocalDateTime now) {
		if (now == null) {
			throw new BadRequestException("now cannot be null");
		}
		return now;
	}

	private Campaign getManagedCampaign(UUID campaignId, CurrentUserInfo currentUser) {
		Campaign campaign = getCampaignEntityById(campaignId);
		syncStatus(campaign, LocalDateTime.now());

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

	private CampaignStatus resolveInitialStatus(LocalDateTime startTime, LocalDateTime now) {
		return startTime.isAfter(now) ? CampaignStatus.PENDING : CampaignStatus.ACTIVE;
	}

	private void syncStatuses(List<Campaign> campaigns, LocalDateTime now) {
		List<Campaign> changedCampaigns = new ArrayList<>();
		for (Campaign campaign : campaigns) {
			if (campaign.syncLifecycleStatus(now)) {
				changedCampaigns.add(campaign);
			}
		}
		if (!changedCampaigns.isEmpty()) {
			campaignRepository.saveAll(changedCampaigns);
		}
	}

	private void syncStatus(Campaign campaign, LocalDateTime now) {
		if (campaign.syncLifecycleStatus(now)) {
			campaignRepository.save(campaign);
		}
	}

}
