package com.affiliateLink.offer.repository;

import com.affiliateLink.config.TestcontainersConfiguration;
import com.affiliateLink.offer.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.docker.compose.enabled=false")
class CampaignRepositoryIT {

    @Autowired CampaignRepository campaignRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired EntityManager entityManager;

    private Offer saveOffer(Long advertiserId) {
        Offer offer = new Offer();
        offer.setAdvertiserId(advertiserId);
        offer.setTitle("Offre test");
        offer.setLandingUrl("https://example.com");
        offer.setNiche("TECH");
        return offerRepository.saveAndFlush(offer);
    }

    private Campaign saveCampaign(Offer offer, String name) {
        Campaign campaign = new Campaign();
        campaign.setName(name);
        campaign.setStartDate(LocalDate.now());
        campaign.setEndDate(LocalDate.now().plusDays(10));
        campaign.setBudget(new BigDecimal("1000"));
        campaign.setPayoutModel(PayoutModel.CPC);
        campaign.setPayoutValue(new BigDecimal("5"));
        offer.addCampaign(campaign);
        return campaignRepository.saveAndFlush(campaign);
    }

    @Test
    void shouldPersistCampaignWithOfferRelationship() {
        Offer offer = saveOffer(10L);
        Campaign saved = saveCampaign(offer, "Campagne A");
        Long id = saved.getId();
        Long offerId = offer.getId();
        entityManager.clear();

        Campaign found = campaignRepository.findById(id).orElseThrow();

        assertThat(found.getName()).isEqualTo("Campagne A");
        assertThat(found.getStatus()).isEqualTo(CampaignStatus.DRAFT);
        assertThat(found.getOffer().getId()).isEqualTo(offerId);
    }

    @Test
    void shouldFindCampaignsByStatus() {
        Offer offer = saveOffer(10L);
        saveCampaign(offer, "Campagne A");
        saveCampaign(offer, "Campagne B");

        assertThat(campaignRepository.findByStatus(CampaignStatus.DRAFT))
                .hasSize(2);
        assertThat(campaignRepository.findByStatus(CampaignStatus.ACTIVE))
                .isEmpty();
    }

    @Test
    void shouldFindActiveCampaignsByOffer() {
        Offer offer = saveOffer(10L);
        Campaign active = saveCampaign(offer, "Active");
        saveCampaign(offer, "Draft");
        active.activate();
        campaignRepository.saveAndFlush(active);

        List<Campaign> result = campaignRepository
                .findByOffer_IdAndStatus(offer.getId(), CampaignStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Active");
    }

    @Test
    void shouldFindActiveCampaignsByAdvertiser() {
        Offer firstOffer = saveOffer(10L);
        Offer secondOffer = saveOffer(20L);

        Campaign first = saveCampaign(firstOffer, "First");
        Campaign second = saveCampaign(secondOffer, "Second");
        first.activate();
        second.activate();
        campaignRepository.flush();

        var result = campaignRepository
                .findByOffer_AdvertiserIdAndStatus(
                        10L, CampaignStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("First");
    }

    @Test
    void shouldRejectTwoActiveCampaignsForSameOffer() {
        Offer offer = saveOffer(10L);
        Campaign first = saveCampaign(offer, "First");
        Campaign second = saveCampaign(offer, "Second");

        first.activate();
        campaignRepository.flush();

        second.activate();

        assertThatThrownBy(campaignRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectInvalidCampaignDateRangeInDatabase() {
        Offer offer = saveOffer(10L);
        Campaign campaign = saveCampaign(offer, "Invalid dates");

        campaign.setEndDate(campaign.getStartDate().minusDays(1));

        assertThatThrownBy(campaignRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectZeroBudgetInDatabase() {
        Offer offer = saveOffer(10L);
        Campaign campaign = saveCampaign(offer, "Invalid budget");

        campaign.setBudget(BigDecimal.ZERO);

        assertThatThrownBy(campaignRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectPercentageAbove100InDatabase() {
        Offer offer = saveOffer(10L);
        Campaign campaign = saveCampaign(offer, "Invalid payout");

        campaign.setPayoutModel(PayoutModel.CPA_PERCENT);
        campaign.setPayoutValue(new BigDecimal("101"));

        assertThatThrownBy(campaignRepository::flush)
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
