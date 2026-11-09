package com.affiliateLink.offer.mapper;

import com.affiliateLink.offer.dto.CampaignResponse;
import com.affiliateLink.offer.dto.CreateCampaignRequest;
import com.affiliateLink.offer.entity.Campaign;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "offer", ignore = true)
    Campaign toEntity(CreateCampaignRequest request);

    @Mapping(target = "offerId", source = "offer.id")
    CampaignResponse toResponse(Campaign campaign);
}
