package com.vouchera.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vouchera.backend.entity.Redemption;
import com.vouchera.backend.enums.RedemptionStatus;

public interface RedemptionRepository extends JpaRepository<Redemption, UUID> {

    List<Redemption> findByUserId(UUID userId);

    boolean existsByUserIdAndVoucherTypeId(UUID userId, UUID voucherTypeId);

    List<Redemption> findByUserIdAndStatus(UUID userId, RedemptionStatus status);
}
