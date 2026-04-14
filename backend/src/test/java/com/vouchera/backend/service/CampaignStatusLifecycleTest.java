package com.vouchera.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vouchera.backend.dto.auth.CurrentUserInfo;
import com.vouchera.backend.dto.campaign.CampaignResponse;
import com.vouchera.backend.entity.Campaign;
import com.vouchera.backend.entity.Company;
import com.vouchera.backend.enums.CampaignStatus;
import com.vouchera.backend.repository.CampaignRepository;
import com.vouchera.backend.repository.CompanyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CampaignStatusLifecycleTest {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private Company company;
    private CurrentUserInfo adminUser;

    @BeforeEach
    void setUp() {
        // Create a test company
        company = new Company("Test Company");
        company.setCompanyStatus(com.vouchera.backend.enums.CompanyStatus.ACTIVE);
        company = companyRepository.save(company);

        // Create mock admin user
        adminUser = new CurrentUserInfo(
            UUID.randomUUID(),
            "admin@test.com",
            com.vouchera.backend.enums.Role.ADMIN,
            com.vouchera.backend.enums.AccountStatus.ACTIVE,
            null
        );
    }

    @Test
    void testCampaignStatusTransitionWhenStartTimeReached() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusSeconds(2);
        LocalDateTime endTime = now.plusHours(1);

        // Create a campaign with a future start time
        CampaignResponse created = campaignService.createCampaign(
            adminUser,
            company.getId(),
            "Future Campaign",
            "Test campaign starting in future",
            startTime,
            endTime
        );

        // Verify status is PENDING
        assertEquals(CampaignStatus.PENDING, created.getStatus());

        // Fetch campaign at the start time (simulating time passing)
        Campaign campaign = campaignRepository.findById(created.getId()).orElseThrow();
        campaign.syncLifecycleStatus(startTime);

        // Verify status changed to ACTIVE
        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());
    }

    @Test
    void testCampaignStatusRetainsPendingBeforeStartTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(10);
        LocalDateTime endTime = now.plusHours(1);

        // Create a campaign with a future start time
        CampaignResponse created = campaignService.createCampaign(
            adminUser,
            company.getId(),
            "Future Campaign",
            "Test campaign",
            startTime,
            endTime
        );

        // Fetch campaign before start time
        Campaign campaign = campaignRepository.findById(created.getId()).orElseThrow();
        boolean changed = campaign.syncLifecycleStatus(now.plusSeconds(5));

        // Verify status is still PENDING (no change)
        assertEquals(CampaignStatus.PENDING, campaign.getStatus());
        assertFalse(changed);
    }

    @Test
    void testManualStatusUpdateLocallyWorks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(5);
        LocalDateTime endTime = now.plusHours(1);

        // Create a campaign
        CampaignResponse created = campaignService.createCampaign(
            adminUser,
            company.getId(),
            "Test Campaign",
            "Test campaign",
            startTime,
            endTime
        );

        // Get the campaign
        Campaign campaign = campaignRepository.findById(created.getId()).orElseThrow();

        // Verify it's PENDING
        assertEquals(CampaignStatus.PENDING, campaign.getStatus());

        // Test pause - should fail because it's not ACTIVE
        assertThrows(IllegalStateException.class, campaign::pause);

        // Test end - should work from any non-ENDED status
        campaign.end();
        assertEquals(CampaignStatus.ENDED, campaign.getStatus());
    }

    @Test
    void testPauseAndResumeTransitions() {
        LocalDateTime now = LocalDateTime.now().plusSeconds(1);  // Add buffer for processing time
        LocalDateTime startTime = now;
        LocalDateTime endTime = now.plusHours(1);

        // Create a campaign that starts now (should be ACTIVE)
        CampaignResponse created = campaignService.createCampaign(
            adminUser,
            company.getId(),
            "Active Campaign",
            "Test campaign",
            startTime,
            endTime
        );

        Campaign campaign = campaignRepository.findById(created.getId()).orElseThrow();

        // Force it to ACTIVE if needed
        campaign.syncLifecycleStatus(now);
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            campaign.setStatus(CampaignStatus.ACTIVE);
        }

        // Pause it
        campaign.pause();
        assertEquals(CampaignStatus.PAUSED, campaign.getStatus());

        // Resume it
        campaign.resume(now);
        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());
    }
}
