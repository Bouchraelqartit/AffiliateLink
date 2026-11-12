package com.affiliateLink.profile.repository;

import com.affiliateLink.config.TestcontainersConfiguration;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.Publisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import com.affiliateLink.profile.entity.Advertiser;
import jakarta.persistence.EntityManager;
import com.affiliateLink.profile.entity.ProfileStatus;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.docker.compose.enabled=false")
class ProfileRepositoryIT {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void shouldReturnTrueWhenEmailAlreadyExists() {
        Publisher publisher = new Publisher(
                "publisher@test.com",
                "Test",
                "Publisher",
                Niche.TECH,
                "Publisher de test",
                "Morocco"
        );
        profileRepository.saveAndFlush(publisher);


        boolean exists = profileRepository.existsByEmail("publisher@test.com");


        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        String email = "unknown@test.com";


        boolean exists = profileRepository.existsByEmail(email);


        assertThat(exists).isFalse();
    }


    @Test
    void shouldRejectDuplicateEmailAcrossProfileTypes() {
        Publisher publisher = new Publisher(
                "duplicate@test.com",
                "Test",
                "Publisher",
                Niche.TECH,
                "Publisher de test",
                "Morocco"
        );
        profileRepository.saveAndFlush(publisher);
        Advertiser advertiser = new Advertiser(
                "duplicate@test.com",
                "Test Company",
                "Technology",
                "https://example.com"
        );


        assertThatThrownBy(() -> profileRepository.saveAndFlush(advertiser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }


    @Test
    void shouldPersistAndRetrievePublisherWithJoinedInheritance() {
        Publisher publisher = new Publisher(
                "joined@test.com",
                "Sara",
                "Test",
                Niche.TECH,
                "Passionnee de technologie",
                "Morocco"
        );
        Long id = profileRepository.saveAndFlush(publisher).getId();
        entityManager.clear();


        Publisher found = (Publisher) profileRepository.findById(id).orElseThrow();


        assertThat(found.getEmail()).isEqualTo("joined@test.com");
        assertThat(found.getStatus()).isEqualTo(ProfileStatus.PENDING);
        assertThat(found.getFirstName()).isEqualTo("Sara");
        assertThat(found.getNiche()).isEqualTo(Niche.TECH);
        assertThat(found.getCountry()).isEqualTo("Morocco");
    }


    @Test
    void shouldPersistAndRetrieveAdvertiserWithJoinedInheritance() {
        Advertiser advertiser = new Advertiser(
                "advertiser-joined@test.com",
                "Tech Company",
                "Technology",
                "https://techcompany.com"
        );
        Long id = profileRepository.saveAndFlush(advertiser).getId();

        entityManager.clear();


        Advertiser found = (Advertiser) profileRepository.findById(id).orElseThrow();


        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getEmail()).isEqualTo("advertiser-joined@test.com");
        assertThat(found.getStatus()).isEqualTo(ProfileStatus.PENDING);
        assertThat(found.getCompanyName()).isEqualTo("Tech Company");
        assertThat(found.getSector()).isEqualTo("Technology");
        assertThat(found.getWebsite()).isEqualTo("https://techcompany.com");
    }

}
