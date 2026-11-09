package com.affiliateLink.offer.listener;

import com.affiliateLink.offer.service.CampaignService;
import com.affiliateLink.profile.api.ProfileSuspended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class ProfileSuspendedListener {

    private static final Logger log = LoggerFactory.getLogger(ProfileSuspendedListener.class);

    private final CampaignService campaignService;

    public ProfileSuspendedListener(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @ApplicationModuleListener
    public void on(ProfileSuspended event) {
        log.info("Received ProfileSuspended event: profileId={}", event.profileId());
        campaignService.pauseActiveCampaignsForAdvertiser(event.profileId());
    }
}
