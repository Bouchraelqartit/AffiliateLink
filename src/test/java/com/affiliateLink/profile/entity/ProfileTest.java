package com.affiliateLink.profile.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.affiliateLink.common.InvalidStatusTransitionException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    @Test
    void shouldRejectSuspendingPendingProfile(){
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"
        );

        assertThatThrownBy(()->profile.changeStatus(ProfileStatus.SUSPENDED))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Cannot change status from PENDING to SUSPENDED");

        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.PENDING);

    }

    @Test
    void shouldRejectMovingActiveProfileToPending() {
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"
        );
        profile.changeStatus(ProfileStatus.ACTIVE);


        assertThatThrownBy(() -> profile.changeStatus(ProfileStatus.PENDING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Cannot change status from ACTIVE to PENDING");

        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
    }


    @Test
    void shouldRejectMovingSuspendedProfileToPending() {
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"
        );
        profile.changeStatus(ProfileStatus.ACTIVE);
        profile.changeStatus(ProfileStatus.SUSPENDED);


        assertThatThrownBy(() -> profile.changeStatus(ProfileStatus.PENDING))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Cannot change status from SUSPENDED to PENDING");

        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.SUSPENDED);
    }

    @ParameterizedTest
    @EnumSource(ProfileStatus.class)
    void shouldRejectTransitionToSameStatus(ProfileStatus status) {
        Profile profile = new Advertiser(
                "advertiser@test.com",
                "tech company",
                "technology",
                "https://example.com"
        );
        if (status == ProfileStatus.ACTIVE) {
            profile.changeStatus(ProfileStatus.ACTIVE);
        }
        if (status == ProfileStatus.SUSPENDED) {
            profile.changeStatus(ProfileStatus.ACTIVE);
            profile.changeStatus(ProfileStatus.SUSPENDED);
        }


        assertThatThrownBy(() -> profile.changeStatus(status))
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(profile.getStatus()).isEqualTo(status);
    }

}
