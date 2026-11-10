package com.affiliateLink.profile.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


public class ProfileTest {
    @Test
    void shouldCreatePublisherWithPendingStatus(){
        String email = "publisher@test.com";

        Publisher publisher = new Publisher(
                email,
                "sarra",
                "test",
                Niche.TECH,
                "developer",
                "morocco"
        );

        assertThat(publisher.getStatus()).isEqualTo(ProfileStatus.PENDING);
        assertThat(publisher.getEmail()).isEqualTo(email);
        assertThat(publisher.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateAdvertiserWithPendingStatus(){
        String email = "advertiser@test.com";

        Advertiser advertiser = new Advertiser(
                email,
                "tech company",
                "technology",
                "https://example.com"
        );

        assertThat(advertiser.getStatus()).isEqualTo(ProfileStatus.PENDING);
        assertThat(advertiser.getEmail()).isEqualTo(email);
        assertThat(advertiser.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldActivatePendingProfile(){
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"

        );

        profile.changeStatus(ProfileStatus.ACTIVE);

        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);

    }

    @Test
    void shouldSuspendActiveProfile(){
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"
        );
        profile.changeStatus(ProfileStatus.ACTIVE);

        profile.changeStatus(ProfileStatus.SUSPENDED);

        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.SUSPENDED);

    }

    @Test
    void shouldReactivateSuspendedProfile(){
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"
        );
        profile.changeStatus(ProfileStatus.ACTIVE);
        profile.changeStatus(ProfileStatus.SUSPENDED);

        profile.changeStatus(ProfileStatus.ACTIVE);

        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
    }

}
