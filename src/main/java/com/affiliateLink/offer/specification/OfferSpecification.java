package com.affiliateLink.offer.specification;

import com.affiliateLink.offer.entity.Offer;
import com.affiliateLink.offer.entity.Tag;
import org.springframework.data.jpa.domain.Specification;

public final class OfferSpecification {

    private OfferSpecification() {
    }

    public static Specification<Offer> hasNiche(String niche) {
        return (root, query, cb) -> niche == null || niche.isBlank()
                ? cb.conjunction()
                : cb.equal(cb.lower(root.get("niche")), niche.toLowerCase());
    }

    public static Specification<Offer> hasAdvertiserId(Long advertiserId) {
        return (root, query, cb) -> advertiserId == null
                ? cb.conjunction()
                : cb.equal(root.get("advertiserId"), advertiserId);
    }

    public static Specification<Offer> hasTag(String tag) {
        return (root, query, cb) -> {
            if (tag == null || tag.isBlank()) return cb.conjunction();
            var tags = root.join("tags");
            query.distinct(true);
            return cb.equal(cb.lower(tags.get("name")), tag.trim().toLowerCase());
        };
    }

    public static Specification<Offer> hasKeyword(String keyword) {
        return (root, query, cb) -> keyword == null || keyword.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
    }
}
