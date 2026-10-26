package com.affiliateLink.profile.service;

import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.dto.AdvertiserResponse;
import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Optional<Profile> getProfileById(Long id) {
        return profileRepository.findById(id);
    }

    public Publisher createPublisher(PublisherRequest request){
        Publisher publisher = new Publisher(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.niche(),
                request.bio(),
                request.country()
        );

        return profileRepository.save(publisher);
    }

    public Advertiser createAdvertiser(AdvertiserRequest request){
        Advertiser advertiser = new Advertiser(
                request.email(),
                request.companyName(),
                request.sector(),
                request.website()
        );

        return profileRepository.save(advertiser);
    }

    public Optional<PublisherResponse> getPublisherById(Long id) {
        return profileRepository.findById(id)
                .filter(profile -> profile instanceof Publisher)
                .map(profile -> (Publisher) profile)
                .map(publisher -> new PublisherResponse(
                        publisher.getId(),
                        publisher.getEmail(),
                        publisher.getStatus(),
                        publisher.getCreatedAt(),
                        publisher.getFirstName(),
                        publisher.getLastName(),
                        publisher.getNiche(),
                        publisher.getBio(),
                        publisher.getCountry()
                ));
    }

    public Optional<AdvertiserResponse> getAdvertiserById(Long id) {
        return profileRepository.findById(id)
                .filter(profile -> profile instanceof Advertiser)
                .map(profile -> (Advertiser) profile)
                .map(advertiser -> new AdvertiserResponse(
                        advertiser.getId(),
                        advertiser.getEmail(),
                        advertiser.getStatus(),
                        advertiser.getCreatedAt(),
                        advertiser.getCompanyName(),
                        advertiser.getSector(),
                        advertiser.getWebsite()
                ));
    }

}
