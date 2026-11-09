package com.affiliateLink.profile.service;

import com.affiliateLink.common.EmailAlreadyExistsException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.api.ProfileApi;
import com.affiliateLink.profile.api.ProfileSuspended;
import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.dto.AdvertiserResponse;
import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfileService implements ProfileApi {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProfileService(ProfileRepository profileRepository, ApplicationEventPublisher eventPublisher) {
        this.profileRepository = profileRepository;
        this.eventPublisher = eventPublisher;
    }

    public PublisherResponse createPublisher(PublisherRequest request) {
        ensureEmailAvailable(request.email());
        Publisher publisher = new Publisher(request.email(), request.firstName(), request.lastName(),
                request.niche(), request.bio(), request.country());
        return toPublisherResponse(profileRepository.save(publisher));
    }

    public AdvertiserResponse createAdvertiser(AdvertiserRequest request) {
        ensureEmailAvailable(request.email());
        Advertiser advertiser = new Advertiser(request.email(), request.companyName(),
                request.sector(), request.website());
        return toAdvertiserResponse(profileRepository.save(advertiser));
    }

    @Transactional(readOnly = true)
    public List<PublisherResponse> getAllPublishers() {
        return profileRepository.findAll().stream()
                .filter(Publisher.class::isInstance)
                .map(Publisher.class::cast)
                .map(this::toPublisherResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PublisherResponse> getPublisherById(Long id) {
        return profileRepository.findById(id)
                .filter(Publisher.class::isInstance)
                .map(Publisher.class::cast)
                .map(this::toPublisherResponse);
    }

    @Transactional(readOnly = true)
    public List<AdvertiserResponse> getAllAdvertisers() {
        return profileRepository.findAll().stream()
                .filter(Advertiser.class::isInstance)
                .map(Advertiser.class::cast)
                .map(this::toAdvertiserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<AdvertiserResponse> getAdvertiserById(Long id) {
        return profileRepository.findById(id)
                .filter(Advertiser.class::isInstance)
                .map(Advertiser.class::cast)
                .map(this::toAdvertiserResponse);
    }

    public void updateProfileStatus(Long id, ProfileStatus newStatus) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));

        ProfileStatus oldStatus = profile.getStatus();
        profile.changeStatus(newStatus);

        if (oldStatus == ProfileStatus.ACTIVE && newStatus == ProfileStatus.SUSPENDED) {
            eventPublisher.publishEvent(new ProfileSuspended(profile.getId()));
            log.info("Profile suspended; suspension event published for profileId={}", profile.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdvertiserActive(Long advertiserId) {
        return profileRepository.findById(advertiserId)
                .filter(Advertiser.class::isInstance)
                .map(Advertiser.class::cast)
                .map(advertiser -> advertiser.getStatus() == ProfileStatus.ACTIVE)
                .orElse(false);
    }

    private void ensureEmailAvailable(String email) {
        if (profileRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private PublisherResponse toPublisherResponse(Publisher publisher) {
        return new PublisherResponse(publisher.getId(), publisher.getEmail(), publisher.getStatus(),
                publisher.getCreatedAt(), publisher.getFirstName(), publisher.getLastName(),
                publisher.getNiche(), publisher.getBio(), publisher.getCountry());
    }

    private AdvertiserResponse toAdvertiserResponse(Advertiser advertiser) {
        return new AdvertiserResponse(advertiser.getId(), advertiser.getEmail(), advertiser.getStatus(),
                advertiser.getCreatedAt(), advertiser.getCompanyName(), advertiser.getSector(),
                advertiser.getWebsite());
    }
}
