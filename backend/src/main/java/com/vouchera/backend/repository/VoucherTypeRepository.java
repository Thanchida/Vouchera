package com.vouchera.backend.repository;

import com.vouchera.backend.entity.VoucherType;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VoucherTypeRepository extends JpaRepository<VoucherType, UUID> {

    List<VoucherType> findByCampaignId(UUID campaignId);
    
    List<VoucherType> findByCampaignIdAndRemainingQuotaGreaterThan(UUID campaignId, Integer remainingQuota);

        @Modifying
        @Query("""
            update VoucherType v
            set v.remainingQuota = v.remainingQuota - 1
            where v.id = :id and v.remainingQuota > 0
        """)
        int decrementQuotaIfAvailable(UUID id);

}