package com.affiliateLink.offer.repository;

import com.affiliateLink.config.TestcontainersConfiguration;
import com.affiliateLink.offer.entity.Offer;
import com.affiliateLink.offer.entity.Tag;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.docker.compose.enabled=false")
class TagRepositoryIT {

    @Autowired TagRepository tagRepository;
    @Autowired OfferRepository offerRepository;
    @Autowired EntityManager entityManager;

    @Test
    void shouldSaveNormalizedTag() {
        Tag tag = tagRepository.saveAndFlush(new Tag("  TeCH  "));

        assertThat(tag.getName()).isEqualTo("tech");
        assertThat(tag.getId()).isNotNull();
    }

    @Test
    void shouldFindTagByName() {
        tagRepository.saveAndFlush(new Tag("spring"));

        assertThat(tagRepository.findByName("spring"))
                .isPresent()
                .get()
                .extracting(Tag::getName)
                .isEqualTo("spring");
    }

    @Test
    void shouldReturnEmptyForUnknownTag() {
        assertThat(tagRepository.findByName("unknown")).isEmpty();
    }

    @Test
    void shouldRejectDuplicateTagName() {
        tagRepository.saveAndFlush(new Tag("JAVA"));

        assertThatThrownBy(() ->
                tagRepository.saveAndFlush(new Tag("java")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldShareOneTagBetweenMultipleOffers() {
        Tag tag = tagRepository.saveAndFlush(new Tag("java"));

        Offer first = new Offer();
        first.setAdvertiserId(10L);
        first.setTitle("Offre Java");
        first.setLandingUrl("https://example.com");
        first.setNiche("TECH");
        first.replaceTags(Set.of(tag));

        Offer second = new Offer();
        second.setAdvertiserId(20L);
        second.setTitle("Offre Spring");
        second.setLandingUrl("https://example.com");
        second.setNiche("TECH");
        second.replaceTags(Set.of(tag));

        Long firstId = offerRepository.saveAndFlush(first).getId();
        Long secondId = offerRepository.saveAndFlush(second).getId();

        entityManager.clear();

        Offer foundFirst = offerRepository.findById(firstId).orElseThrow();
        Offer foundSecond = offerRepository.findById(secondId).orElseThrow();

        assertThat(foundFirst.getTags())
                .extracting(Tag::getName)
                .containsExactly("java");

        assertThat(foundSecond.getTags())
                .extracting(Tag::getName)
                .containsExactly("java");

        assertThat(tagRepository.count()).isEqualTo(1);
    }
}
