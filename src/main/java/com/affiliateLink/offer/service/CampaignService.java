package com.affiliateLink.offer.service;

import com.affiliateLink.common.BusinessConflictException;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.dto.CampaignResponse;
import com.affiliateLink.offer.dto.CreateCampaignRequest;
import com.affiliateLink.offer.entity.Campaign;
import com.affiliateLink.offer.entity.CampaignStatus;
import com.affiliateLink.offer.entity.Offer;
import com.affiliateLink.offer.mapper.CampaignMapper;
import com.affiliateLink.offer.repository.CampaignRepository;
import com.affiliateLink.offer.repository.OfferRepository;
import com.affiliateLink.profile.api.ProfileApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);

    private final CampaignRepository campaignRepository;
    private final OfferRepository offerRepository;
    private final CampaignMapper campaignMapper;
    private final ProfileApi profileApi;

    public CampaignService(CampaignRepository campaignRepository, OfferRepository offerRepository,
                           CampaignMapper campaignMapper, ProfileApi profileApi) {
        this.campaignRepository = campaignRepository;
        this.offerRepository = offerRepository;
        this.campaignMapper = campaignMapper;
        this.profileApi = profileApi;
    }

    public CampaignResponse create(Long offerId, CreateCampaignRequest request) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        if (!profileApi.isAdvertiserActive(offer.getAdvertiserId())) {
            throw new BusinessConflictException("Offer advertiser does not exist or is not ACTIVE");
        }

        Campaign campaign = campaignMapper.toEntity(request);
        offer.addCampaign(campaign);
        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign created: campaignId={}, offerId={}", saved.getId(), offerId);
        return campaignMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> findByStatus(CampaignStatus status) {
        List<Campaign> campaigns = status == null
                ? campaignRepository.findAll()
                : campaignRepository.findByStatus(status);
        return campaigns.stream().map(campaignMapper::toResponse).toList();
    }

    public void updateStatus(Long campaignId, CampaignStatus requestedStatus) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        try {
            if (requestedStatus == CampaignStatus.ACTIVE) {
                activateAtomically(campaign);
                return;
            }
            switch (requestedStatus) {
                case PAUSED -> campaign.pause();
                case ENDED -> campaign.end();
                case DRAFT -> throw new BusinessConflictException("A campaign cannot return to DRAFT");
                case ACTIVE -> throw new IllegalStateException("Handled above");
            }
            log.info("Campaign status changed: campaignId={}, status={}", campaignId, requestedStatus);
        } catch (InvalidStatusTransitionException | BusinessConflictException ex) {
            log.warn("Campaign status rejected: id={}, requested={}, reason={}",
                    campaignId, requestedStatus, ex.getMessage());
            throw ex;
        }
    }

    private void activateAtomically(Campaign campaign) {
        Long offerId = campaign.getOffer().getId();
        offerRepository.findByIdForUpdate(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        List<Campaign> activeCampaigns =
                campaignRepository.findByOffer_IdAndStatus(offerId, CampaignStatus.ACTIVE);

        for (Campaign active : activeCampaigns) {
            if (!active.getId().equals(campaign.getId())) {
                active.pause();
                log.info("Existing campaign paused before activation: campaignId={}", active.getId());
            }
        }

        campaignRepository.flush();
        campaign.activate();
        log.info("Campaign activated successfully: campaignId={}, offerId={}", campaign.getId(), offerId);
    }

    public void pauseActiveCampaignsForAdvertiser(Long advertiserId) {
        List<Campaign> activeCampaigns =
                campaignRepository.findByOffer_AdvertiserIdAndStatus(advertiserId, CampaignStatus.ACTIVE);

        for (Campaign campaign : activeCampaigns) {
            campaign.pause();
            log.info("Campaign paused after advertiser suspension: campaignId={}, advertiserId={}",
                    campaign.getId(), advertiserId);
        }
    }
}
