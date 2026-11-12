package com.affiliateLink.offer.entity;

import com.affiliateLink.common.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class CampaignTest {

    private Campaign campaign() {
        Campaign campaign = new Campaign();
        campaign.setStartDate(LocalDate.now());
        campaign.setEndDate(LocalDate.now().plusDays(10));
        return campaign;
    }

    @Test
    void shouldStartInDraft() {
        assertThat(campaign().getStatus())
                .isEqualTo(CampaignStatus.DRAFT);
    }

    @Test
    void shouldActivateDraftCampaign() {
        Campaign campaign = campaign();

        campaign.activate();

        assertThat(campaign.getStatus())
                .isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldPauseActiveCampaign() {
        Campaign campaign = campaign();
        campaign.activate();

        campaign.pause();

        assertThat(campaign.getStatus())
                .isEqualTo(CampaignStatus.PAUSED);
    }

    @Test
    void shouldReactivatePausedCampaign() {
        Campaign campaign = campaign();
        campaign.activate();
        campaign.pause();

        campaign.activate();

        assertThat(campaign.getStatus())
                .isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldEndActiveCampaign() {
        Campaign campaign = campaign();
        campaign.activate();

        campaign.end();

        assertThat(campaign.getStatus())
                .isEqualTo(CampaignStatus.ENDED);
    }

    @Test
    void shouldEndPausedCampaign() {
        Campaign campaign = campaign();
        campaign.activate();
        campaign.pause();

        campaign.end();

        assertThat(campaign.getStatus())
                .isEqualTo(CampaignStatus.ENDED);
    }

    @Test
    void shouldRejectExpiredCampaignActivation() {
        Campaign campaign = campaign();
        campaign.setEndDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(campaign::activate)
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(campaign.getStatus())
                .isEqualTo(CampaignStatus.DRAFT);
    }

    @ParameterizedTest
    @EnumSource(CampaignStatus.class)
    void shouldRejectPauseUnlessActive(CampaignStatus status) {
        Campaign campaign = campaign();

        switch (status) {
            case ACTIVE -> campaign.activate();
            case PAUSED -> {
                campaign.activate();
                campaign.pause();
            }
            case ENDED -> {
                campaign.activate();
                campaign.end();
            }
            case DRAFT -> {}
        }

        if (status == CampaignStatus.ACTIVE) {
            campaign.pause();
            assertThat(campaign.getStatus())
                    .isEqualTo(CampaignStatus.PAUSED);
        } else {
            assertThatThrownBy(campaign::pause)
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }

    @ParameterizedTest
    @EnumSource(CampaignStatus.class)
    void shouldRejectActivationFromInvalidStatuses(CampaignStatus status) {
        Campaign campaign = campaign();

        switch (status) {
            case ACTIVE -> campaign.activate();
            case PAUSED -> {
                campaign.activate();
                campaign.pause();
            }
            case ENDED -> {
                campaign.activate();
                campaign.end();
            }
            case DRAFT -> {}
        }

        if (status == CampaignStatus.ACTIVE
                || status == CampaignStatus.ENDED) {
            assertThatThrownBy(campaign::activate)
                    .isInstanceOf(InvalidStatusTransitionException.class);
        } else {
            campaign.activate();
            assertThat(campaign.getStatus())
                    .isEqualTo(CampaignStatus.ACTIVE);
        }
    }

    @ParameterizedTest
    @EnumSource(CampaignStatus.class)
    void shouldAllowEndOnlyFromActiveOrPaused(CampaignStatus status) {
        Campaign campaign = campaign();

        switch (status) {
            case ACTIVE -> campaign.activate();
            case PAUSED -> {
                campaign.activate();
                campaign.pause();
            }
            case ENDED -> {
                campaign.activate();
                campaign.end();
            }
            case DRAFT -> {}
        }

        if (status == CampaignStatus.ACTIVE
                || status == CampaignStatus.PAUSED) {
            campaign.end();
            assertThat(campaign.getStatus())
                    .isEqualTo(CampaignStatus.ENDED);
        } else {
            assertThatThrownBy(campaign::end)
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }
}
