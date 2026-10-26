
package com.affiliateLink.profile.dto;

import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.ProfileStatus;
import java.time.LocalDateTime;

public record PublisherResponse(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt,
        String firstName,
        String lastName,
        Niche niche,
        String bio,
        String country
) {
}
