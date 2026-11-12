package com.affiliateLink.offer.repository;

import com.affiliateLink.config.TestcontainersConfiguration;
import com.affiliateLink.offer.entity.Offer;
import com.affiliateLink.offer.entity.Tag;
import com.affiliateLink.offer.specification.OfferSpecification;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.docker.compose.enabled=false")
class OfferRepositoryIT {

    @Autowired OfferRepository offerRepository;
    @Autowired TagRepository tagRepository;
    @Autowired EntityManager entityManager;

    private Offer saveOffer(Long advertiserId, String title, String niche) {
        Offer offer = new Offer();
        offer.setAdvertiserId(advertiserId);
        offer.setTitle(title);
        offer.setDescription("Description de test");
        offer.setLandingUrl("https://example.com");
        offer.setNiche(niche);
        return offerRepository.saveAndFlush(offer);
    }

    @Test
    void shouldPersistAndRetrieveOffer() {
        Offer saved = saveOffer(10L, "Formation Java", "TECH");
        entityManager.clear();

        Offer found = offerRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("Formation Java");
        assertThat(found.getAdvertiserId()).isEqualTo(10L);
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindOfferByIdAndAdvertiserId() {
        Offer offer = saveOffer(10L, "Java", "TECH");

        assertThat(offerRepository.findByIdAndAdvertiserId(
                offer.getId(), 10L)).isPresent();

        assertThat(offerRepository.findByIdAndAdvertiserId(
                offer.getId(), 99L)).isEmpty();
    }

    @Test
    void shouldFindOffersByAdvertiser() {
        saveOffer(10L, "Java", "TECH");
        saveOffer(10L, "Spring", "TECH");
        saveOffer(20L, "Voyage", "TRAVEL");

        assertThat(offerRepository.findAllByAdvertiserId(10L))
                .hasSize(2);
    }

    @Test
    void shouldFindOfferWithPessimisticLock() {
        Offer saved = saveOffer(10L, "Java", "TECH");

        Offer locked = offerRepository
                .findByIdForUpdate(saved.getId())
                .orElseThrow();

        assertThat(locked.getId()).isEqualTo(saved.getId());
    }

    @Test
    void shouldFilterByNiche() {
        saveOffer(10L, "Java", "TECH");
        saveOffer(20L, "Voyage", "TRAVEL");

        var result = offerRepository.findAll(
                OfferSpecification.hasNiche("tech"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java");
    }

    @Test
    void shouldFilterByAdvertiserId() {
        saveOffer(10L, "Java", "TECH");
        saveOffer(20L, "Voyage", "TRAVEL");

        var result = offerRepository.findAll(
                OfferSpecification.hasAdvertiserId(10L));

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFilterByKeyword() {
        saveOffer(10L, "Formation Spring Boot", "TECH");
        saveOffer(20L, "Voyage Maroc", "TRAVEL");

        var result = offerRepository.findAll(
                OfferSpecification.hasKeyword("SPRING"));

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFilterByTag() {
        Tag tag = tagRepository.saveAndFlush(new Tag("java"));
        Offer offer = saveOffer(10L, "Formation Java", "TECH");
        offer.replaceTags(Set.of(tag));
        offerRepository.saveAndFlush(offer);
        entityManager.clear();

        var result = offerRepository.findAll(
                OfferSpecification.hasTag(" JAVA "));

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCombineAllSearchCriteria() {
        Tag tag = tagRepository.saveAndFlush(new Tag("spring"));

        Offer matching = saveOffer(10L, "Formation Spring", "TECH");
        matching.replaceTags(Set.of(tag));
        offerRepository.saveAndFlush(matching);

        saveOffer(20L, "Formation Java", "TECH");
        saveOffer(10L, "Voyage Maroc", "TRAVEL");

        Specification<Offer> specification =
                Specification.where(OfferSpecification.hasNiche("TECH"))
                        .and(OfferSpecification.hasAdvertiserId(10L))
                        .and(OfferSpecification.hasTag("spring"))
                        .and(OfferSpecification.hasKeyword("formation"));

        var result = offerRepository.findAll(specification);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(matching.getId());
    }

    @Test
    void shouldReturnAllOffersWithoutFilters() {
        saveOffer(10L, "Java", "TECH");
        saveOffer(20L, "Voyage", "TRAVEL");

        Specification<Offer> specification =
                Specification.where(OfferSpecification.hasNiche(null))
                        .and(OfferSpecification.hasAdvertiserId(null))
                        .and(OfferSpecification.hasTag(null))
                        .and(OfferSpecification.hasKeyword(null));

        assertThat(offerRepository.findAll(specification)).hasSize(2);
    }

    @Test
    void shouldSupportPaginationAndSorting() {
        saveOffer(10L, "Charlie", "TECH");
        saveOffer(10L, "Alpha", "TECH");
        saveOffer(10L, "Bravo", "TECH");

        Page<Offer> page = offerRepository.findAll(
                OfferSpecification.hasNiche("TECH"),
                PageRequest.of(0, 2, Sort.by("title").ascending())
        );

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent())
                .extracting(Offer::getTitle)
                .containsExactly("Alpha", "Bravo");
    }
}
