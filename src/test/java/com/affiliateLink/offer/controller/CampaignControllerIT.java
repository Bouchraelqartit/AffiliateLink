package com.affiliateLink.offer.controller;

import com.affiliateLink.common.BusinessConflictException;
import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.dto.CampaignResponse;
import com.affiliateLink.offer.dto.CreateCampaignRequest;
import com.affiliateLink.offer.entity.CampaignStatus;
import com.affiliateLink.offer.entity.PayoutModel;
import com.affiliateLink.offer.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CampaignController.class)
class CampaignControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CampaignService campaignService;

    private CampaignResponse response() {
        return new CampaignResponse(
                5L, "Campagne Java",
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 12, 12),
                new BigDecimal("1000"),
                PayoutModel.CPC,
                new BigDecimal("5"),
                CampaignStatus.DRAFT,
                1L
        );
    }

    private String validCreateJson() {
        return """
                {
                  "name": "Campagne Java",
                  "startDate": "2026-11-12",
                  "endDate": "2026-12-12",
                  "budget": 1000,
                  "payoutModel": "CPC",
                  "payoutValue": 5
                }
                """;
    }

    @Test
    void shouldReturn201WhenCreatingCampaign() throws Exception {
        when(campaignService.create(eq(1L),
                any(CreateCampaignRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/offres/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.offerId").value(1));

        verify(campaignService).create(eq(1L),
                any(CreateCampaignRequest.class));
    }

    @Test
    void shouldReturn400ForInvalidDateRange() throws Exception {
        mockMvc.perform(post("/api/offres/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Campagne",
                                  "startDate": "2026-12-12",
                                  "endDate": "2026-11-12",
                                  "budget": 1000,
                                  "payoutModel": "CPC",
                                  "payoutValue": 5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(campaignService, never()).create(anyLong(), any());
    }

    @Test
    void shouldReturn400ForZeroBudget() throws Exception {
        mockMvc.perform(post("/api/offres/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Campagne",
                                  "startDate": "2026-11-12",
                                  "endDate": "2026-12-12",
                                  "budget": 0,
                                  "payoutModel": "CPC",
                                  "payoutValue": 5
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(campaignService, never()).create(anyLong(), any());
    }

    @Test
    void shouldReturn400ForInvalidPayout() throws Exception {
        mockMvc.perform(post("/api/offres/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Campagne",
                                  "startDate": "2026-11-12",
                                  "endDate": "2026-12-12",
                                  "budget": 1000,
                                  "payoutModel": "CPA_PERCENT",
                                  "payoutValue": 150
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(campaignService, never()).create(anyLong(), any());
    }

    @Test
    void shouldReturn404ForUnknownOffer() throws Exception {
        when(campaignService.create(eq(99L),
                any(CreateCampaignRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Offer not found with id: 99"));

        mockMvc.perform(post("/api/offres/99/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn409ForInactiveAdvertiser() throws Exception {
        when(campaignService.create(eq(1L),
                any(CreateCampaignRequest.class)))
                .thenThrow(new BusinessConflictException(
                        "Offer advertiser does not exist or is not ACTIVE"));

        mockMvc.perform(post("/api/offres/1/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldListAllCampaigns() throws Exception {
        when(campaignService.findByStatus(null))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    void shouldFilterCampaignsByStatus() throws Exception {
        when(campaignService.findByStatus(CampaignStatus.DRAFT))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/campaigns")
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Campagne Java"));

        verify(campaignService).findByStatus(CampaignStatus.DRAFT);
    }

    @Test
    void shouldReturn204WhenUpdatingStatus() throws Exception {
        mockMvc.perform(patch("/api/campaigns/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE"}
                                """))
                .andExpect(status().isNoContent());

        verify(campaignService).updateStatus(5L, CampaignStatus.ACTIVE);
    }

    @Test
    void shouldReturn400ForMissingStatus() throws Exception {
        mockMvc.perform(patch("/api/campaigns/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(campaignService, never())
                .updateStatus(anyLong(), any());
    }

    @Test
    void shouldReturn404ForUnknownCampaign() throws Exception {
        doThrow(new ResourceNotFoundException(
                "Campaign not found with id: 99"))
                .when(campaignService)
                .updateStatus(99L, CampaignStatus.ACTIVE);

        mockMvc.perform(patch("/api/campaigns/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn409ForInvalidTransition() throws Exception {
        doThrow(new InvalidStatusTransitionException(
                "Cannot activate campaign from ENDED"))
                .when(campaignService)
                .updateStatus(5L, CampaignStatus.ACTIVE);

        mockMvc.perform(patch("/api/campaigns/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Cannot activate campaign from ENDED"));
    }
}
