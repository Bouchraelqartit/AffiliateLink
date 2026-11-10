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

}
