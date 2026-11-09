package com.affiliateLink.offer.dto;

import com.affiliateLink.offer.entity.CampaignStatus;

import java.time.LocalDate;

public record CampaignSummaryResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        CampaignStatus status
) {
}
