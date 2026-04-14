package com.vouchera.backend.dto.campaign;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.enums.CampaignStatus;

public class CampaignResponse {

    private UUID id;
    private String name;
    private String description;
    private CampaignStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private CompanyResponse company;

    public CampaignResponse(
        UUID id,
        String name,
        String description,
        CampaignStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        CompanyResponse company
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.company = company;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public CompanyResponse getCompany() {
        return company;
    }

    public static CampaignResponse fromEntity(Campaign campaign) {
        if (campaign == null) {
            return null;
        }

        return new CampaignResponse(
            campaign.getId(),
            campaign.getName(),
            campaign.getDescription(),
            campaign.getStatus(),
            campaign.getStartTime(),
            campaign.getEndTime(),
            CompanyResponse.fromEntity(campaign.getCompany())
        );
    }
}