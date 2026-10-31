package com.affiliateLink.profile.service;

import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.dto.AdvertiserResponse;
import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.affiliateLink.common.EmailAlreadyExistsException;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.common.InvalidStatusTransitionException;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public PublisherResponse createPublisher(PublisherRequest request) {
        Publisher publisher = new Publisher(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.niche(),
                request.bio(),
                request.country()
        );

        return toPublisherResponse(profileRepository.save(publisher));
    }

    public AdvertiserResponse createAdvertiser(AdvertiserRequest request) {
        Advertiser advertiser = new Advertiser(
                request.email(),
                request.companyName(),
                request.sector(),
                request.website()
        );

        return toAdvertiserResponse(profileRepository.save(advertiser));
    }

    public List<PublisherResponse> getAllPublishers() {
        return profileRepository.findAll()
                .stream()
                .filter(Publisher.class::isInstance)
                .map(Publisher.class::cast)
                .map(this::toPublisherResponse)
                .toList();
    }

    public Optional<PublisherResponse> getPublisherById(Long id) {
        return profileRepository.findById(id)
                .filter(Publisher.class::isInstance)
                .map(Publisher.class::cast)
                .map(this::toPublisherResponse);
    }

    public List<AdvertiserResponse> getAllAdvertisers() {
        return profileRepository.findAll()
                .stream()
                .filter(Advertiser.class::isInstance)
                .map(Advertiser.class::cast)
                .map(this::toAdvertiserResponse)
                .toList();
    }

    public Optional<AdvertiserResponse> getAdvertiserById(Long id) {
        return profileRepository.findById(id)
                .filter(Advertiser.class::isInstance)
                .map(Advertiser.class::cast)
                .map(this::toAdvertiserResponse);
    }

    private PublisherResponse toPublisherResponse(Publisher publisher) {
        return new PublisherResponse(
                publisher.getId(),
                publisher.getEmail(),
                publisher.getStatus(),
                publisher.getCreatedAt(),
                publisher.getFirstName(),
                publisher.getLastName(),
                publisher.getNiche(),
                publisher.getBio(),
                publisher.getCountry()
        );
    }

    private AdvertiserResponse toAdvertiserResponse(Advertiser advertiser) {
        return new AdvertiserResponse(
                advertiser.getId(),
                advertiser.getEmail(),
                advertiser.getStatus(),
                advertiser.getCreatedAt(),
                advertiser.getCompanyName(),
                advertiser.getSector(),
                advertiser.getWebsite()
        );
    }

    @Transactional
    public void updateProfileStatus(Long id, ProfileStatus newStatus) {

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Profile not found with id: " + id
                ));

        try {
            profile.changeStatus(newStatus);
        } catch (IllegalStateException exception) {
            throw new InvalidStatusTransitionException(exception.getMessage());
        }
    }
}
