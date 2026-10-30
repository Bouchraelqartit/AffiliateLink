package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.dto.AdvertiserResponse;
import com.affiliateLink.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisers")
public class AdvertiserController {

    private final ProfileService profileService;

    public AdvertiserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<AdvertiserResponse> createAdvertiser(
            @Valid @RequestBody AdvertiserRequest request) {

        AdvertiserResponse response =
                profileService.createAdvertiser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<AdvertiserResponse> getAllAdvertisers() {
        return profileService.getAllAdvertisers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertiserResponse> getAdvertiserById(
            @PathVariable Long id) {

        return profileService.getAdvertiserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}