
package com.affiliateLink.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record AdvertiserRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String companyName,

        @NotBlank
        String sector,

        @NotBlank
        String website

) {
}
