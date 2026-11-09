package com.affiliateLink.offer.repository;

import com.affiliateLink.offer.entity.Campaign;
import com.affiliateLink.offer.entity.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByStatus(CampaignStatus status);
    List<Campaign> findByOffer_IdAndStatus(Long offerId, CampaignStatus status);
    List<Campaign> findByOffer_AdvertiserIdAndStatus(Long advertiserId, CampaignStatus status);
}
