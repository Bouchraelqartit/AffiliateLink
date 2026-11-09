package com.affiliateLink.offer.controller;

import com.affiliateLink.offer.dto.CreateOfferRequest;
import com.affiliateLink.offer.dto.OfferResponse;
import com.affiliateLink.offer.dto.UpdateOfferRequest;
import com.affiliateLink.offer.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offres")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    public ResponseEntity<OfferResponse> create(@Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(request));
    }

    @GetMapping
    public Page<OfferResponse> search(
            @RequestParam(required = false) String niche,
            @RequestParam(required = false) Long advertiserId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page must be >= 0 and size must be between 1 and 100");
        }

        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.ASC;

        if (!property.matches("id|title|niche|createdAt|advertiserId")) {
            throw new IllegalArgumentException("Unsupported sort property: " + property);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        return offerService.search(niche, advertiserId, tag, keyword, pageable);
    }

    @GetMapping("/{id}")
    public OfferResponse getById(@PathVariable Long id) {
        return offerService.getById(id);
    }

    @PutMapping("/{id}")
    public OfferResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOfferRequest request) {
        return offerService.update(id, request);
    }
}
