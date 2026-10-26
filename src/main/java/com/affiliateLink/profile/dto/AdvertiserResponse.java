package com.affiliateLink.profile.dto;

import com.affiliateLink.profile.entity.ProfileStatus;
import java.time.LocalDateTime;

public record AdvertiserResponse(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String companyName,
        String sector,
        String website
) {
}