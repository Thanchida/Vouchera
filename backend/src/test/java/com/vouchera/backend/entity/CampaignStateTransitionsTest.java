package com.vouchera.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.vouchera.backend.enums.CampaignStatus;

class CampaignStateTransitionsTest {

    @Test
    void pauseChangesStatusFromActiveToPaused() {
        Campaign campaign = newCampaign(CampaignStatus.ACTIVE, LocalDateTime.now().plusDays(3));

        campaign.pause();

        assertEquals(CampaignStatus.PAUSED, campaign.getStatus());
    }

    @Test
    void pauseThrowsWhenCampaignNotActive() {
        Campaign campaign = newCampaign(CampaignStatus.PAUSED, LocalDateTime.now().plusDays(3));

        assertThrows(IllegalStateException.class, campaign::pause);
    }

    @Test
    void resumeChangesStatusFromPausedToActiveBeforeEndTime() {
        Campaign campaign = newCampaign(CampaignStatus.PAUSED, LocalDateTime.now().plusDays(3));

        campaign.resume(LocalDateTime.now());

        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());
    }

    @Test
    void resumeThrowsWhenCampaignAlreadyEndedByTime() {
        Campaign campaign = newCampaign(CampaignStatus.PAUSED, LocalDateTime.now().plusHours(2));

        assertThrows(IllegalStateException.class, () -> campaign.resume(LocalDateTime.now().plusHours(3)));
    }

    @Test
    void endChangesStatusToEnded() {
        Campaign campaign = newCampaign(CampaignStatus.ACTIVE, LocalDateTime.now().plusDays(3));

        campaign.end();

        assertEquals(CampaignStatus.ENDED, campaign.getStatus());
    }

    @Test
    void endThrowsWhenAlreadyEnded() {
        Campaign campaign = newCampaign(CampaignStatus.ENDED, LocalDateTime.now().plusDays(3));

        assertThrows(IllegalStateException.class, campaign::end);
    }

    @Test
    void updateDetailsThrowsWhenCampaignEnded() {
        Campaign campaign = newCampaign(CampaignStatus.ENDED, LocalDateTime.now().plusDays(3));

        assertThrows(IllegalStateException.class,
                () -> campaign.updateDetails("New Name", "Updated", LocalDateTime.now().plusDays(4),
                        LocalDateTime.now().plusDays(6)));
    }

    @Test
    void pendingCampaignBecomesActiveWhenStartTimeIsReached() {
        LocalDateTime now = LocalDateTime.now();
        Campaign campaign = new Campaign(
                "Campaign",
                "Description",
                new Company("Acme"),
                now.plusMinutes(10),
                now.plusHours(2),
                CampaignStatus.PENDING);

        boolean changed = campaign.syncLifecycleStatus(now.plusMinutes(10));

        assertTrue(changed);
        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());
    }

    @Test
    void pendingCampaignIsNotRedeemableBeforeStartTime() {
        LocalDateTime now = LocalDateTime.now();
        Campaign campaign = new Campaign(
                "Campaign",
                "Description",
                new Company("Acme"),
                now.plusMinutes(10),
                now.plusHours(2),
                CampaignStatus.PENDING);

        assertFalse(campaign.isActiveAt(now.plusMinutes(5)));
    }

    private Campaign newCampaign(CampaignStatus status, LocalDateTime endTime) {
        Company company = new Company("Acme");
        return new Campaign(
                "Campaign",
                "Description",
                company,
                LocalDateTime.now().plusHours(1),
                endTime,
                status);
    }
}
