package com.affiliateLink.offer.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateOfferRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private CreateOfferRequest request(
            Long advertiserId,
            String title,
            String description,
            String url,
            String niche,
            Set<String> tags) {

        return new CreateOfferRequest(
                advertiserId, title, description,
                url, niche, tags
        );
    }

    private void assertViolation(
            CreateOfferRequest request,
            String property) {

        assertThat(validator.validate(request))
                .anySatisfy(violation ->
                        assertThat(violation.getPropertyPath().toString())
                                .contains(property));
    }

    @Test
    void shouldAcceptValidOffer() {
        var request = request(
                1L, "Formation Java", "Description",
                "https://example.com", "TECH",
                Set.of("java", "spring")
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -10})
    void shouldRejectNonPositiveAdvertiserId(long id) {
        assertViolation(request(
                id, "Offer", "Description",
                "https://example.com", "TECH", Set.of()
        ), "advertiserId");
    }

    @Test
    void shouldRejectMissingAdvertiserId() {
        assertViolation(request(
                null, "Offer", "Description",
                "https://example.com", "TECH", Set.of()
        ), "advertiserId");
    }

    @Test
    void shouldRejectBlankTitle() {
        assertViolation(request(
                1L, " ", "Description",
                "https://example.com", "TECH", Set.of()
        ), "title");
    }

    @Test
    void shouldRejectLongTitle() {
        assertViolation(request(
                1L, "A".repeat(151), "Description",
                "https://example.com", "TECH", Set.of()
        ), "title");
    }

    @Test
    void shouldRejectLongDescription() {
        assertViolation(request(
                1L, "Offer", "A".repeat(5001),
                "https://example.com", "TECH", Set.of()
        ), "description");
    }

    @Test
    void shouldRejectBlankLandingUrl() {
        assertViolation(request(
                1L, "Offer", "Description",
                " ", "TECH", Set.of()
        ), "landingUrl");
    }

    @Test
    void shouldRejectLongLandingUrl() {
        assertViolation(request(
                1L, "Offer", "Description",
                "a".repeat(2049), "TECH", Set.of()
        ), "landingUrl");
    }

    @Test
    void shouldRejectBlankNiche() {
        assertViolation(request(
                1L, "Offer", "Description",
                "https://example.com", " ", Set.of()
        ), "niche");
    }

    @Test
    void shouldRejectLongNiche() {
        assertViolation(request(
                1L, "Offer", "Description",
                "https://example.com", "A".repeat(101), Set.of()
        ), "niche");
    }

    @Test
    void shouldRejectBlankTag() {
        assertViolation(request(
                1L, "Offer", "Description",
                "https://example.com", "TECH", Set.of(" ")
        ), "tags");
    }

    @Test
    void shouldRejectLongTag() {
        assertViolation(request(
                1L, "Offer", "Description",
                "https://example.com", "TECH",
                Set.of("a".repeat(51))
        ), "tags");
    }
}
