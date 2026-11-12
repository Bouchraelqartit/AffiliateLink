package com.affiliateLink.offer.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class OfferTest {

    @Test
    void shouldLinkCampaignToOffer() {
        // Given
        Offer offer = new Offer();
        Campaign campaign = new Campaign();

        // When
        offer.addCampaign(campaign);

        // Then
        assertThat(offer.getCampaigns()).contains(campaign);
        assertThat(campaign.getOffer()).isSameAs(offer);
    }

    @Test
    void shouldReplaceTags() {
        Offer offer = new Offer();
        Tag oldTag = new Tag("tech");
        Tag newTag = new Tag("gaming");

        offer.replaceTags(Set.of(oldTag));
        offer.replaceTags(Set.of(newTag));

        assertThat(offer.getTags())
                .containsExactly(newTag);
    }

    @Test
    void shouldClearTagsWhenNull() {
        Offer offer = new Offer();
        offer.replaceTags(Set.of(new Tag("tech")));

        offer.replaceTags(null);

        assertThat(offer.getTags()).isEmpty();
    }

    @Test
    void shouldInitializeCollectionsAndCreationDate() {
        Offer offer = new Offer();

        assertThat(offer.getTags()).isEmpty();
        assertThat(offer.getCampaigns()).isEmpty();
        assertThat(offer.getCreatedAt()).isNotNull();
    }
}
