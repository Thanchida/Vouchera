package com.vouchera.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.enums.CampaignStatus;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    List<Campaign> findByCompanyId(UUID companyId);

    List<Campaign> findByCompanyIdAndStatus(UUID companyId, CampaignStatus status);

    Optional<Campaign> findByIdAndStatus(UUID id, CampaignStatus status);
    
    List<Campaign> findByStatusAndStartTimeBeforeAndEndTimeAfter(CampaignStatus status, LocalDateTime now1, LocalDateTime now2);

    Page<Campaign> findByStatusAndStartTimeBeforeAndEndTimeAfter(
        CampaignStatus status,
        LocalDateTime now1,
        LocalDateTime now2,
        Pageable pageable
    );

}
