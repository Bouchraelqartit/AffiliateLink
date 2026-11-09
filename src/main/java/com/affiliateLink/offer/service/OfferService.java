package com.affiliateLink.offer.service;

import com.affiliateLink.common.BusinessConflictException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.dto.CreateOfferRequest;
import com.affiliateLink.offer.dto.OfferResponse;
import com.affiliateLink.offer.dto.UpdateOfferRequest;
import com.affiliateLink.offer.entity.Offer;
import com.affiliateLink.offer.entity.Tag;
import com.affiliateLink.offer.mapper.OfferMapper;
import com.affiliateLink.offer.repository.OfferRepository;
import com.affiliateLink.offer.repository.TagRepository;
import com.affiliateLink.offer.specification.OfferSpecification;
import com.affiliateLink.profile.api.ProfileApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@Transactional
public class OfferService {

    private static final Logger log = LoggerFactory.getLogger(OfferService.class);

    private final OfferRepository offerRepository;
    private final TagRepository tagRepository;
    private final OfferMapper offerMapper;
    private final ProfileApi profileApi;

    public OfferService(OfferRepository offerRepository, TagRepository tagRepository,
                        OfferMapper offerMapper, ProfileApi profileApi) {
        this.offerRepository = offerRepository;
        this.tagRepository = tagRepository;
        this.offerMapper = offerMapper;
        this.profileApi = profileApi;
    }

    public OfferResponse create(CreateOfferRequest request) {
        if (!profileApi.isAdvertiserActive(request.advertiserId())) {
            throw new BusinessConflictException("Advertiser does not exist or is not ACTIVE");
        }

        Offer offer = new Offer();
        offer.setAdvertiserId(request.advertiserId());
        offer.setTitle(request.title());
        offer.setDescription(request.description());
        offer.setLandingUrl(request.landingUrl());
        offer.setNiche(request.niche().trim());
        offer.replaceTags(resolveTags(request.tags()));

        Offer saved = offerRepository.save(offer);
        log.info("Offer created successfully: offerId={}, advertiserId={}", saved.getId(), saved.getAdvertiserId());
        return offerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponse> search(String niche, Long advertiserId, String tag, String keyword, Pageable pageable) {
        Specification<Offer> specification = Specification
                .where(OfferSpecification.hasNiche(niche))
                .and(OfferSpecification.hasAdvertiserId(advertiserId))
                .and(OfferSpecification.hasTag(tag))
                .and(OfferSpecification.hasKeyword(keyword));

        return offerRepository.findAll(specification, pageable).map(offerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OfferResponse getById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));
        return offerMapper.toResponse(offer);
    }

    public OfferResponse update(Long id, UpdateOfferRequest request) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + id));

        offer.setTitle(request.title());
        offer.setDescription(request.description());
        offer.setLandingUrl(request.landingUrl());
        offer.setNiche(request.niche().trim());
        offer.replaceTags(resolveTags(request.tags()));

        Offer saved = offerRepository.save(offer);
        log.info("Offer updated successfully: offerId={}", id);
        return offerMapper.toResponse(saved);
    }

    private Set<Tag> resolveTags(Set<String> names) {
        Set<Tag> resolved = new LinkedHashSet<>();
        if (names == null) return resolved;

        for (String name : names) {
            String normalized = Tag.normalize(name);
            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> tagRepository.save(new Tag(normalized)));
            resolved.add(tag);
        }
        return resolved;
    }
}
