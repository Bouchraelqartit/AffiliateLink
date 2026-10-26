
package com.affiliateLink.profile.dto;

import com.affiliateLink.profile.entity.Niche;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublisherRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull
        Niche niche,

        @Size(max = 500)
        String bio,

        @NotBlank
        String country

) {
}
