package com.affiliateLink.offer.entity;

import com.affiliateLink.common.InvalidStatusTransitionException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_model", nullable = false, length = 30)
    private PayoutModel payoutModel;

    @Column(name = "payout_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal payoutValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    public Campaign() {
    }

    public void setOffer(Offer offer) { this.offer = offer; }

    public void activate() {
        if (status != CampaignStatus.DRAFT && status != CampaignStatus.PAUSED) {
            throw new InvalidStatusTransitionException("Cannot activate campaign from " + status);
        }
        if (endDate.isBefore(LocalDate.now())) {
            throw new InvalidStatusTransitionException("An expired campaign cannot be activated");
        }
        status = CampaignStatus.ACTIVE;
    }

    public void pause() {
        if (status != CampaignStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only an ACTIVE campaign can be paused");
        }
        status = CampaignStatus.PAUSED;
    }

    public void end() {
        if (status != CampaignStatus.ACTIVE && status != CampaignStatus.PAUSED) {
            throw new InvalidStatusTransitionException("Cannot end campaign from " + status);
        }
        status = CampaignStatus.ENDED;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getBudget() { return budget; }
    public PayoutModel getPayoutModel() { return payoutModel; }
    public BigDecimal getPayoutValue() { return payoutValue; }
    public CampaignStatus getStatus() { return status; }
    public Offer getOffer() { return offer; }
    public void setName(String name) { this.name = name; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public void setPayoutModel(PayoutModel payoutModel) { this.payoutModel = payoutModel; }
    public void setPayoutValue(BigDecimal payoutValue) { this.payoutValue = payoutValue; }
}
