package com.affiliateLink.offer.service;

import com.affiliateLink.common.BusinessConflictException;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.dto.CreateCampaignRequest;
import com.affiliateLink.offer.entity.*;
import com.affiliateLink.offer.mapper.CampaignMapper;
import com.affiliateLink.offer.repository.CampaignRepository;
import com.affiliateLink.offer.repository.OfferRepository;
import com.affiliateLink.profile.api.ProfileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock CampaignRepository campaignRepository;
    @Mock OfferRepository offerRepository;
    @Mock CampaignMapper campaignMapper;
    @Mock ProfileApi profileApi;

    @InjectMocks CampaignService service;

    private Offer offer() {
        Offer offer = new Offer();
        offer.setAdvertiserId(10L);
        return offer;
    }

    private Campaign campaign() {
        Campaign campaign = new Campaign();
        campaign.setStartDate(LocalDate.now());
        campaign.setEndDate(LocalDate.now().plusDays(10));
        return campaign;
    }

    private CreateCampaignRequest request() {
        return new CreateCampaignRequest(
                "Campagne test",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                PayoutModel.CPC,
                new BigDecimal("5")
        );
    }

    @Test
    void shouldCreateCampaignForActiveAdvertiser() {
        Offer offer = offer();
        Campaign campaign = campaign();

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileApi.isAdvertiserActive(10L)).thenReturn(true);
        when(campaignMapper.toEntity(any())).thenReturn(campaign);
        when(campaignRepository.save(campaign)).thenReturn(campaign);

        service.create(1L, request());

        assertThat(campaign.getOffer()).isSameAs(offer);
        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
        verify(campaignRepository).save(campaign);
    }

    @Test
    void shouldRejectCampaignForMissingOffer() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(99L, request()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void shouldRejectCampaignForInactiveAdvertiser() {
        Offer offer = offer();
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(profileApi.isAdvertiserActive(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(1L, request()))
                .isInstanceOf(BusinessConflictException.class);

        verify(campaignRepository, never()).save(any());
    }

    @Test
    void shouldListAllCampaignsWhenStatusIsNull() {
        Campaign campaign = campaign();
        when(campaignRepository.findAll()).thenReturn(List.of(campaign));

        service.findByStatus(null);

        verify(campaignRepository).findAll();
        verify(campaignRepository, never()).findByStatus(any());
    }

    @Test
    void shouldFilterCampaignsByStatus() {
        when(campaignRepository.findByStatus(CampaignStatus.DRAFT))
                .thenReturn(List.of(campaign()));

        service.findByStatus(CampaignStatus.DRAFT);

        verify(campaignRepository).findByStatus(CampaignStatus.DRAFT);
    }

    @Test
    void shouldRejectStatusUpdateForMissingCampaign() {
        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(99L, CampaignStatus.ACTIVE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @ParameterizedTest
    @EnumSource(value = CampaignStatus.class,
            names = {"PAUSED", "ENDED"})
    void shouldRejectInvalidTransitionFromDraft(CampaignStatus requested) {
        Campaign campaign = campaign();
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.updateStatus(1L, requested))
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
    }

    @Test
    void shouldRejectReturnToDraft() {
        when(campaignRepository.findById(1L))
                .thenReturn(Optional.of(campaign()));

        assertThatThrownBy(() -> service.updateStatus(1L, CampaignStatus.DRAFT))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void shouldPauseActiveCampaign() {
        Campaign campaign = campaign();
        campaign.activate();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        service.updateStatus(1L, CampaignStatus.PAUSED);

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.PAUSED);
    }

    @Test
    void shouldEndActiveCampaign() {
        Campaign campaign = campaign();
        campaign.activate();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        service.updateStatus(1L, CampaignStatus.ENDED);

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ENDED);
    }

    @Test
    void shouldEndPausedCampaign() {
        Campaign campaign = campaign();
        campaign.activate();
        campaign.pause();

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        service.updateStatus(1L, CampaignStatus.ENDED);

        assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.ENDED);
    }

    @Test
    void shouldPauseCampaignsAfterAdvertiserSuspension() {
        Campaign first = campaign();
        Campaign second = campaign();
        first.activate();
        second.activate();

        when(campaignRepository.findByOffer_AdvertiserIdAndStatus(
                10L, CampaignStatus.ACTIVE))
                .thenReturn(List.of(first, second));

        service.pauseActiveCampaignsForAdvertiser(10L);

        assertThat(first.getStatus()).isEqualTo(CampaignStatus.PAUSED);
        assertThat(second.getStatus()).isEqualTo(CampaignStatus.PAUSED);
    }

    @Test
    void shouldDoNothingWhenNoActiveCampaignsExist() {
        when(campaignRepository.findByOffer_AdvertiserIdAndStatus(
                10L, CampaignStatus.ACTIVE))
                .thenReturn(List.of());

        service.pauseActiveCampaignsForAdvertiser(10L);

        verify(campaignRepository).findByOffer_AdvertiserIdAndStatus(
                10L, CampaignStatus.ACTIVE);
    }
}
