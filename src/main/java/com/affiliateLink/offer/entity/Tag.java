package com.affiliateLink.offer.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "offer_tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Offer> offers = new HashSet<>();

    protected Tag() {
    }

    public Tag(String name) {
        this.name = normalize(name);
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 50) {
            throw new IllegalArgumentException("Tag name must not exceed 50 characters");
        }
        return normalized;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<Offer> getOffers() { return offers; }
}
