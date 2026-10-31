
package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.UpdateProfileStatusRequest;
import com.affiliateLink.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/profiles")
public class AdminProfileController {

    private final ProfileService profileService;

    public AdminProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateProfileStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileStatusRequest request) {

        profileService.updateProfileStatus(id, request.status());

        return ResponseEntity.noContent().build();
    }
}
