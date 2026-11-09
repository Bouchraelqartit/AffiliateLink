package com.affiliateLink.offer.controller;

import com.affiliateLink.offer.dto.CampaignResponse;
import com.affiliateLink.offer.dto.CreateCampaignRequest;
import com.affiliateLink.offer.dto.UpdateCampaignStatusRequest;
import com.affiliateLink.offer.entity.CampaignStatus;
import com.affiliateLink.offer.service.CampaignService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping("/api/offres/{offerId}/campaigns")
    public ResponseEntity<CampaignResponse> create(
            @PathVariable Long offerId,
            @Valid @RequestBody CreateCampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(offerId, request));
    }

    @GetMapping("/api/campaigns")
    public List<CampaignResponse> findByStatus(@RequestParam(required = false) CampaignStatus status) {
        return campaignService.findByStatus(status);
    }

    @PatchMapping("/api/campaigns/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCampaignStatusRequest request) {
        campaignService.updateStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }
}
