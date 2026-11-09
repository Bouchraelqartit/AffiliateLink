package com.affiliateLink.offer.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;

public record OfferResponse(
        Long id,
        Long advertiserId,
        String title,
        String description,
        String landingUrl,
        String niche,
        LocalDateTime createdAt,
        Set<String> tags,
        List<CampaignSummaryResponse> campaigns
) {
}
