
package com.affiliateLink.profile.dto;

import com.affiliateLink.profile.entity.ProfileStatus;
import java.time.LocalDateTime;

public record ProfileResponse(
        Long id,
        String email,
        ProfileStatus status,
        LocalDateTime createdAt
) {
}
