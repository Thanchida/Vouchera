package com.vouchera.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.dto.CurrentUserInfo;
import com.vouchera.backend.dto.redemption.RedemptionResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Redemption;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.entity.VoucherType;
import com.vouchera.backend.enums.RedemptionStatus;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
import com.vouchera.backend.mapper.RedemptionMapper;
import com.vouchera.backend.repository.RedemptionRepository;
import com.vouchera.backend.repository.UserRepository;
import com.vouchera.backend.repository.VoucherTypeRepository;

@Service
@Transactional
public class RedemptionService {

	private final RedemptionRepository redemptionRepository;
	private final UserRepository userRepository;
	private final VoucherTypeRepository voucherTypeRepository;

	public RedemptionService(RedemptionRepository redemptionRepository,
							 UserRepository userRepository,
							 VoucherTypeRepository voucherTypeRepository) {
		this.redemptionRepository = redemptionRepository;
		this.userRepository = userRepository;
		this.voucherTypeRepository = voucherTypeRepository;
	}

	public RedemptionResponse redeemCoupon(CurrentUserInfo currentUser, UUID voucherTypeId, LocalDateTime now) {
		LocalDateTime current = requireNow(now);
		User user = getUserById(currentUser.userId());

		VoucherType voucherType = voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found"));

		Campaign campaign = voucherType.getCampaign();
		if (!campaign.isActiveAt(current)) {
			throw new IllegalStateException("Campaign is not active for redemption");
		}

		if (redemptionRepository.existsByUserIdAndVoucherTypeId(user.getId(), voucherTypeId)) {
			throw new IllegalStateException("User has already redeemed this voucher type");
		}

		voucherType.claimOne();
		voucherTypeRepository.save(voucherType);

		Redemption redemption = new Redemption(user, voucherType);
		try {
			return RedemptionMapper.toResponse(redemptionRepository.save(redemption));
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalStateException("Duplicate redemption is not allowed", ex);
		}
	}

	@Transactional(readOnly = true)
	public List<RedemptionResponse> getUserRedemptions(UUID userId, CurrentUserInfo currentUser) {
		ensureUserAccess(userId, currentUser);
		return RedemptionMapper.toResponseList(redemptionRepository.findByUserId(userId));
	}

	@Transactional(readOnly = true)
	public List<RedemptionResponse> getUserRedemptionsByStatus(UUID userId, CurrentUserInfo currentUser, RedemptionStatus status) {
		ensureUserAccess(userId, currentUser);
		if (status == null) {
			throw new BadRequestException("Redemption status is required");
		}
		return RedemptionMapper.toResponseList(redemptionRepository.findByUserIdAndStatus(userId, status));
	}

	public RedemptionResponse markRedemptionUsed(UUID redemptionId, CurrentUserInfo currentUser) {
		Redemption redemption = getRedemptionById(redemptionId);

		Campaign campaign = redemption.getVoucherType().getCampaign();
		if (!canManageCampaign(currentUser, campaign)) {
			throw new ForbiddenException("User is not allowed to mark this redemption as used");
		}

		redemption.markUsed();
		return RedemptionMapper.toResponse(redemptionRepository.save(redemption));
	}

	public RedemptionResponse expireRedemption(UUID redemptionId, CurrentUserInfo currentUser) {
		Redemption redemption = getRedemptionById(redemptionId);

		Campaign campaign = redemption.getVoucherType().getCampaign();
		if (!canManageCampaign(currentUser, campaign)) {
			throw new ForbiddenException("User is not allowed to expire this redemption");
		}

		redemption.markExpired();
		return RedemptionMapper.toResponse(redemptionRepository.save(redemption));
	}

	private void ensureUserAccess(UUID userId, CurrentUserInfo currentUser) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User not found");
		}
		if (!currentUser.isAdmin() && !currentUser.userId().equals(userId)) {
			throw new ForbiddenException("User is not allowed to access these redemptions");
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

	private User getUserById(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException("User not found"));
	}

	private Redemption getRedemptionById(UUID redemptionId) {
		return redemptionRepository.findById(redemptionId)
			.orElseThrow(() -> new NotFoundException("Redemption not found"));
	}

	private LocalDateTime requireNow(LocalDateTime now) {
		if (now == null) {
			throw new BadRequestException("now cannot be null");
		}
		return now;
	}
}
