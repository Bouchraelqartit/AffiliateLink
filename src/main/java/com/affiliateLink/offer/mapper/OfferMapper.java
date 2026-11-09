package com.affiliateLink.offer.mapper;

import com.affiliateLink.offer.dto.CampaignSummaryResponse;
import com.affiliateLink.offer.dto.OfferResponse;
import com.affiliateLink.offer.entity.Campaign;
import com.affiliateLink.offer.entity.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfferMapper {

    @Mapping(target = "tags", expression = "java(offer.getTags().stream().map(com.affiliateLink.offer.entity.Tag::getName).collect(java.util.stream.Collectors.toSet()))")
    @Mapping(target = "campaigns", expression = "java(offer.getCampaigns().stream().map(this::toCampaignSummary).toList())")
    OfferResponse toResponse(Offer offer);

    CampaignSummaryResponse toCampaignSummary(Campaign campaign);
}
