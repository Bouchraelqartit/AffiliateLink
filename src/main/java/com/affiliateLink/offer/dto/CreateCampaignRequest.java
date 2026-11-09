package com.affiliateLink.offer.dto;

import com.affiliateLink.offer.entity.PayoutModel;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCampaignRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin(value = "0.01") BigDecimal budget,
        @NotNull PayoutModel payoutModel,
        @NotNull @DecimalMin(value = "0.00") BigDecimal payoutValue
) {
    @AssertTrue(message = "endDate must be after startDate")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }

    @AssertTrue(message = "payoutValue must be greater than zero for CPC, CPL and CPA_FIXED, and between 0 and 100 for CPA_PERCENT")
    public boolean isPayoutValueValid() {
        if (payoutModel == null || payoutValue == null) return true;
        if (payoutModel == PayoutModel.CPA_PERCENT) {
            return payoutValue.compareTo(BigDecimal.ZERO) >= 0
                    && payoutValue.compareTo(new BigDecimal("100")) <= 0;
        }
        return payoutValue.compareTo(BigDecimal.ZERO) > 0;
    }
}
