package com.vouchera.backend.mapper;

import java.util.List;

import com.vouchera.backend.dto.campaign.CampaignResponse;
import com.vouchera.backend.dto.company.CompanyResponse;
import com.vouchera.backend.entity.Campaign;

public final class CampaignMapper {

    private CampaignMapper() {
    }

    public static CampaignResponse toResponse(Campaign campaign) {
        CompanyResponse company = campaign.getCompany() == null
            ? null
            : new CompanyResponse(
                campaign.getCompany().getId(),
                campaign.getCompany().getName(),
                campaign.getCompany().getCompanyStatus()
            );

        return new CampaignResponse(
            campaign.getId(),
            campaign.getName(),
            campaign.getDescription(),
            campaign.getStatus(),
            campaign.getStartTime(),
            campaign.getEndTime(),
            company
        );
    }

    public static List<CampaignResponse> toResponseList(List<Campaign> campaigns) {
        return campaigns.stream().map(CampaignMapper::toResponse).toList();
    }
}