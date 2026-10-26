package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.entity.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.entity.Advertiser;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public List<Profile> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/publishers")
    public ResponseEntity<Publisher> createPublisher(
            @RequestBody PublisherRequest request) {

        Publisher publisher = profileService.createPublisher(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(publisher);
    }

    @PostMapping("/advertisers")
    public ResponseEntity<Advertiser> createAdvertiser(
            @RequestBody AdvertiserRequest request) {

        Advertiser advertiser = profileService.createAdvertiser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(advertiser);
    }
}
