package com.affiliateLink.offer.dto;

import com.affiliateLink.offer.entity.PayoutModel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCampaignRequestTest {

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

    private CreateCampaignRequest request(
            LocalDate start,
            LocalDate end,
            BigDecimal budget,
            PayoutModel model,
            BigDecimal payout) {

        return new CreateCampaignRequest(
                "Test Campaign",
                start,
                end,
                budget,
                model,
                payout
        );
    }

    private CreateCampaignRequest validRequest() {
        return request(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                PayoutModel.CPC,
                new BigDecimal("5")
        );
    }

    private void assertViolation(
            CreateCampaignRequest request,
            String property) {

        assertThat(validator.validate(request))
                .anySatisfy(violation ->
                        assertThat(violation.getPropertyPath().toString())
                                .isEqualTo(property));
    }

    @Test
    void shouldAcceptValidCampaign() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        CreateCampaignRequest request = request(
                LocalDate.now(),
                LocalDate.now().minusDays(1),
                new BigDecimal("1000"),
                PayoutModel.CPC,
                new BigDecimal("5")
        );

        assertViolation(request, "dateRangeValid");
    }

    @Test
    void shouldRejectEqualStartAndEndDates() {
        LocalDate date = LocalDate.now();

        assertViolation(request(
                date, date, new BigDecimal("1000"),
                PayoutModel.CPC, new BigDecimal("5")
        ), "dateRangeValid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100"})
    void shouldRejectNonPositiveBudget(String amount) {
        CreateCampaignRequest request = request(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal(amount),
                PayoutModel.CPC,
                new BigDecimal("5")
        );

        assertViolation(request, "budget");
    }

    @ParameterizedTest
    @EnumSource(value = PayoutModel.class,
            names = {"CPC", "CPL", "CPA_FIXED"})
    void shouldRejectZeroPayoutForFixedModels(PayoutModel model) {
        assertViolation(request(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                model,
                BigDecimal.ZERO
        ), "payoutValueValid");
    }

    @ParameterizedTest
    @EnumSource(value = PayoutModel.class,
            names = {"CPC", "CPL", "CPA_FIXED"})
    void shouldAcceptPositivePayoutForFixedModels(PayoutModel model) {
        assertThat(validator.validate(request(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                model,
                new BigDecimal("5")
        ))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "25", "100"})
    void shouldAcceptValidPercentagePayout(String value) {
        assertThat(validator.validate(request(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                PayoutModel.CPA_PERCENT,
                new BigDecimal(value)
        ))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "100.01", "150"})
    void shouldRejectInvalidPercentagePayout(String value) {
        assertViolation(request(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                PayoutModel.CPA_PERCENT,
                new BigDecimal(value)
        ), "payoutValueValid");
    }

    @Test
    void shouldRejectMissingRequiredFields() {
        CreateCampaignRequest request = new CreateCampaignRequest(
                null, null, null, null, null, null
        );

        var violations = validator.validate(request);

        assertThat(violations).hasSize(6);
    }

    @Test
    void shouldRejectBlankName() {
        CreateCampaignRequest request = new CreateCampaignRequest(
                " ",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                PayoutModel.CPC,
                new BigDecimal("5")
        );

        assertViolation(request, "name");
    }

    @Test
    void shouldRejectNameLongerThan150Characters() {
        CreateCampaignRequest request = new CreateCampaignRequest(
                "A".repeat(151),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                new BigDecimal("1000"),
                PayoutModel.CPC,
                new BigDecimal("5")
        );

        assertViolation(request, "name");
    }
}
