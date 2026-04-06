package com.vouchera.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.enums.AccountStatus;
import com.vouchera.backend.enums.CampaignStatus;
import com.vouchera.backend.enums.CompanyStatus;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.repository.CampaignRepository;
import com.vouchera.backend.repository.CompanyRepository;
import com.vouchera.backend.repository.UserRepository;

@Service
@Transactional
public class CampaignService {

	private final CampaignRepository campaignRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;

	public CampaignService(CampaignRepository campaignRepository, 
		CompanyRepository companyRepository, UserRepository userRepository) {
		this.campaignRepository = campaignRepository;
		this.companyRepository = companyRepository;
		this.userRepository = userRepository;
	}

	public Campaign createCampaign(UUID actorUserId, UUID companyId, 
		String name,
		String description,
		LocalDateTime startTime,
		LocalDateTime endTime) {
		validateCampaignInput(name, description, startTime, endTime);
		validateCreateWindow(startTime, LocalDateTime.now());

		User actor = getUserById(actorUserId);
		validateCampaignCreator(actor, companyId);

		Company company = companyRepository.findById(companyId)
			.orElseThrow(() -> new NotFoundException("Company not found"));

		if (company.getCompanyStatus() != CompanyStatus.ACTIVE) {
			throw new IllegalStateException("Cannot create a campaign for a non-ACTIVE company");
		}

		Campaign campaign = new Campaign(
			name.trim(), description.trim(), company, 
			startTime, endTime, CampaignStatus.ACTIVE
		);

		return campaignRepository.save(campaign);
	}

	@Transactional(readOnly = true)
	public List<Campaign> listCampaigns() {
		return campaignRepository.findAll();
	}

	// @Transactional(readOnly = true)
	// public Page<Campaign> listCampaigns(Pageable pageable) {
	// 	return campaignRepository.findAll(pageable);
	// }

	@Transactional(readOnly = true)
	public Campaign getCampaignById(UUID campaignId) {
		return campaignRepository.findById(campaignId)
			.orElseThrow(() -> new NotFoundException("Campaign not found"));
	}

	@Transactional(readOnly = true)
	public List<Campaign> listActiveCampaigns(LocalDateTime now) {
		LocalDateTime current = requireNow(now);
		return campaignRepository.findByStatusAndStartTimeBeforeAndEndTimeAfter(
			CampaignStatus.ACTIVE, current, current);
	}

	// @Transactional(readOnly = true)
	// public Page<Campaign> listActiveCampaigns(LocalDateTime now, Pageable pageable) {
	// 	LocalDateTime current = requireNow(now);
	// 	return campaignRepository.findByStatusAndStartTimeBeforeAndEndTimeAfter(
	// 		CampaignStatus.ACTIVE, current, current, pageable);
	// }

	@Transactional(readOnly = true)
	public List<Campaign> listCompanyCampaigns(UUID companyId, CampaignStatus status) {
		if (status == null) {
			return campaignRepository.findByCompanyId(companyId);
		}
		return campaignRepository.findByCompanyIdAndStatus(companyId, status);
	}

	public Campaign pauseCampaign(UUID campaignId, UUID actorUserId) {
		Campaign campaign = getManagedCampaign(campaignId, actorUserId);
		campaign.pause();
		return campaignRepository.save(campaign);
	}

	public Campaign resumeCampaign(UUID campaignId, UUID actorUserId, LocalDateTime now) {
		Campaign campaign = getManagedCampaign(campaignId, actorUserId);
		campaign.resume(requireNow(now));
		return campaignRepository.save(campaign);
	}

	public Campaign endCampaign(UUID campaignId, UUID actorUserId) {
		Campaign campaign = getManagedCampaign(campaignId, actorUserId);
		campaign.end();
		return campaignRepository.save(campaign);
	}

	public Campaign updateCampaign(UUID campaignId, 
		UUID actorUserId,
		String name,
		String description,
		LocalDateTime startTime,
		LocalDateTime endTime) {
		Campaign campaign = getManagedCampaign(campaignId, actorUserId);

		if (!campaign.canBeEdited()) {
			throw new IllegalStateException("Only ACTIVE or PAUSED campaigns can be edited");
		}

		validateCampaignInput(name, description, startTime, endTime);

		campaign.setName(name.trim());
		campaign.setDescription(description.trim());
		campaign.setStartTime(startTime);
		campaign.setEndTime(endTime);
		return campaignRepository.save(campaign);
	}

	public void deleteCampaign(UUID campaignId, UUID actorUserId) {
		Campaign campaign = getManagedCampaign(campaignId, actorUserId);
		campaignRepository.delete(campaign);
	}

	private User getUserById(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException("User not found"));
	}

	private LocalDateTime requireNow(LocalDateTime now) {
		if (now == null) {
			throw new BadRequestException("now cannot be null");
		}
		return now;
	}

	private Campaign getManagedCampaign(UUID campaignId, UUID actorUserId) {
		Campaign campaign = getCampaignById(campaignId);
		User actor = getUserById(actorUserId);

		validateCampaignManager(actor, campaign);
		return campaign;
	}

	private void validateCampaignManager(User actor, Campaign campaign) {
		if (actor.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new ForbiddenException("Account is suspended");
		}
		if (!actor.canManageCampaign(campaign)) {
			throw new ForbiddenException("User is not allowed to manage this campaign");
		}
	}

	private void validateCampaignCreator(User actor, UUID companyId) {
		if (actor.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new ForbiddenException("Account is suspended");
		}
		if (actor.isAdmin()) {
			return;
		}
		if (!actor.isMarketing()) {
			throw new ForbiddenException("Only ADMIN or MARKETING can create campaigns");
		}
		if (actor.getCompany() == null || !actor.getCompany().getId().equals(companyId)) {
			throw new ForbiddenException("MARKETING user can only create campaigns for their own company");
		}
	}

	private void validateCreateWindow(LocalDateTime startTime, LocalDateTime now) {
		if (startTime.isBefore(now)) {
			throw new BadRequestException("Campaign startTime cannot be in the past");
		}
	}

	private void validateCampaignInput(String name, 
		String description,
		LocalDateTime startTime,
		LocalDateTime endTime) {
		if (name == null || name.isBlank()) {
			throw new BadRequestException("Campaign name cannot be blank");
		}
		if (description == null || description.isBlank()) {
			throw new BadRequestException("Campaign description cannot be blank");
		}
		if (startTime == null || endTime == null) {
			throw new BadRequestException("Campaign startTime and endTime are required");
		}
		if (!endTime.isAfter(startTime)) {
			throw new BadRequestException("Campaign endTime must be after startTime");
		}
	}
}
