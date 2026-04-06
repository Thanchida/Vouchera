package com.vouchera.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Redemption;
import com.vouchera.backend.entity.User;
import com.vouchera.backend.entity.VoucherType;
import com.vouchera.backend.enums.RedemptionStatus;
import com.vouchera.backend.exception.BadRequestException;
import com.vouchera.backend.exception.ForbiddenException;
import com.vouchera.backend.exception.NotFoundException;
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

	public Redemption redeemCoupon(UUID userId, UUID voucherTypeId, LocalDateTime now) {
		LocalDateTime current = requireNow(now);
		User user = getUserById(userId);

		VoucherType voucherType = voucherTypeRepository.findById(voucherTypeId)
			.orElseThrow(() -> new NotFoundException("Voucher type not found"));

		Campaign campaign = voucherType.getCampaign();
		if (!campaign.isActiveAt(current)) {
			throw new IllegalStateException("Campaign is not active for redemption");
		}

		if (redemptionRepository.existsByUserIdAndVoucherTypeId(userId, voucherTypeId)) {
			throw new IllegalStateException("User has already redeemed this voucher type");
		}

		voucherType.claimOne();
		voucherTypeRepository.save(voucherType);

		Redemption redemption = new Redemption(user, voucherType);
		try {
			return redemptionRepository.save(redemption);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalStateException("Duplicate redemption is not allowed", ex);
		}
	}

	@Transactional(readOnly = true)
	public List<Redemption> getUserRedemptions(UUID userId) {
		ensureUserExists(userId);
		return redemptionRepository.findByUserId(userId);
	}

	@Transactional(readOnly = true)
	public List<Redemption> getUserRedemptionsByStatus(UUID userId, RedemptionStatus status) {
		ensureUserExists(userId);
		if (status == null) {
			throw new BadRequestException("Redemption status is required");
		}
		return redemptionRepository.findByUserIdAndStatus(userId, status);
	}

	public Redemption markRedemptionUsed(UUID redemptionId, UUID actorUserId) {
		Redemption redemption = getRedemptionById(redemptionId);
		User actor = getUserById(actorUserId);

		Campaign campaign = redemption.getVoucherType().getCampaign();
		if (!actor.canManageCampaign(campaign)) {
			throw new ForbiddenException("User is not allowed to mark this redemption as used");
		}

		redemption.markUsed();
		return redemptionRepository.save(redemption);
	}

	public Redemption expireRedemption(UUID redemptionId) {
		Redemption redemption = getRedemptionById(redemptionId);
		redemption.markExpired();
		return redemptionRepository.save(redemption);
	}

	private User getUserById(UUID userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new NotFoundException("User not found"));
	}

	private void ensureUserExists(UUID userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("User not found");
		}
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
