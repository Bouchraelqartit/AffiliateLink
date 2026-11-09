package com.affiliateLink.offer.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertiser_id", nullable = false)
    private Long advertiserId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "landing_url", nullable = false, length = 2048)
    private String landingUrl;

    @Column(nullable = false, length = 100)
    private String niche;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany
    @JoinTable(name = "offer_tag_links",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Campaign> campaigns = new HashSet<>();

    public Offer() {
    }

    public void addCampaign(Campaign campaign) {
        campaigns.add(campaign);
        campaign.setOffer(this);
    }

    public void replaceTags(Set<Tag> newTags) {
        tags.clear();
        if (newTags != null) {
            tags.addAll(newTags);
        }
    }

    public Long getId() { return id; }
    public Long getAdvertiserId() { return advertiserId; }
    public void setAdvertiserId(Long advertiserId) { this.advertiserId = advertiserId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLandingUrl() { return landingUrl; }
    public void setLandingUrl(String landingUrl) { this.landingUrl = landingUrl; }
    public String getNiche() { return niche; }
    public void setNiche(String niche) { this.niche = niche; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<Tag> getTags() { return tags; }
    public Set<Campaign> getCampaigns() { return campaigns; }
}
