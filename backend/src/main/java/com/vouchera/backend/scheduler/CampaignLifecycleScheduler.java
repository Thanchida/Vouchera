package com.vouchera.backend.scheduler;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.vouchera.backend.service.CampaignService;

@Component
public class CampaignLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(CampaignLifecycleScheduler.class);

    private final CampaignService campaignService;

    public CampaignLifecycleScheduler(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Scheduled(fixedDelayString = "${campaign.status-sync.fixed-delay-ms:30000}")
    public void syncCampaignStatuses() {
        int changed = campaignService.syncAllLifecycleStatuses(LocalDateTime.now());
        if (changed > 0) {
            log.info("Campaign lifecycle sync updated {} campaign(s)", changed);
        }
    }
}
