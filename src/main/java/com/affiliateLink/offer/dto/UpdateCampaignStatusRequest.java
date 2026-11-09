package com.affiliateLink.offer.dto;

import com.affiliateLink.offer.entity.CampaignStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCampaignStatusRequest(@NotNull CampaignStatus status) {
}
