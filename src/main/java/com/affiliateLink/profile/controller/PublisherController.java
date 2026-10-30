package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final ProfileService profileService;

    public PublisherController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<PublisherResponse> createPublisher(
            @Valid @RequestBody PublisherRequest request) {

        PublisherResponse response =
                profileService.createPublisher(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<PublisherResponse> getAllPublishers() {
        return profileService.getAllPublishers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponse> getPublisherById(
            @PathVariable Long id) {

        return profileService.getPublisherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}