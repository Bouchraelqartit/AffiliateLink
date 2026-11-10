package com.affiliateLink.profile.service;

import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.dto.AdvertiserResponse;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.common.EmailAlreadyExistsException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void shouldCreatePublisherWithPendingStatus() {
        PublisherRequest request = new PublisherRequest(
                "publisher@test.com",
                "Sarra",
                "Test",
                Niche.TECH,
                "Content creator",
                "Morocco"
        );
        when(profileRepository.existsByEmail(request.email())).thenReturn(false);
        when(profileRepository.save(any(Publisher.class))).thenAnswer(invocation -> invocation.getArgument(0));


        PublisherResponse response = profileService.createPublisher(request);


        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ProfileStatus.PENDING);
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.firstName()).isEqualTo(request.firstName());
        verify(profileRepository).existsByEmail(request.email());
        verify(profileRepository).save(any(Publisher.class));
    }

    @Test
    void shouldCreateAdvertiserWithPendingStatus() {
        AdvertiserRequest request = new AdvertiserRequest(
                "advertiser@test.com",
                "Tech Company",
                "Technology",
                "https://example.com"
        );
        when(profileRepository.existsByEmail(request.email())).thenReturn(false);
        when(profileRepository.save(any(Advertiser.class))).thenAnswer(invocation -> invocation.getArgument(0));


        AdvertiserResponse response = profileService.createAdvertiser(request);


        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ProfileStatus.PENDING);
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.companyName()).isEqualTo(request.companyName());
        assertThat(response.sector()).isEqualTo(request.sector());
        assertThat(response.website()).isEqualTo(request.website());
        verify(profileRepository).existsByEmail(request.email());
        verify(profileRepository).save(any(Advertiser.class));
    }

    @Test
    void shouldRejectPublisherCreationWhenEmailAlreadyExists() {
        PublisherRequest request = new PublisherRequest(
                "existing@test.com",
                "Sarra",
                "Test",
                Niche.TECH,
                "Content creator",
                "Morocco"
        );
        when(profileRepository.existsByEmail(request.email())).thenReturn(true);


        assertThatThrownBy(() -> profileService.createPublisher(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: " + request.email());
        verify(profileRepository).existsByEmail(request.email());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void shouldRejectAdvertiserCreationWhenEmailAlreadyExists() {
        AdvertiserRequest request = new AdvertiserRequest(
                "existing@test.com",
                "Tech Company",
                "Technology",
                "https://example.com"
        );
        when(profileRepository.existsByEmail(request.email())).thenReturn(true);


        assertThatThrownBy(() -> profileService.createAdvertiser(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: " + request.email());
        verify(profileRepository).existsByEmail(request.email());
        verify(profileRepository, never()).save(any());
    }
}