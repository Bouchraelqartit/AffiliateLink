package com.affiliateLink.offer.controller;

import com.affiliateLink.common.BusinessConflictException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.dto.CreateOfferRequest;
import com.affiliateLink.offer.dto.OfferResponse;
import com.affiliateLink.offer.dto.UpdateOfferRequest;
import com.affiliateLink.offer.service.OfferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfferController.class)
class OfferControllerIT {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OfferService offerService;

    private OfferResponse response() {
        return new OfferResponse(
                1L, 10L, "Formation Java", "Description",
                "https://example.com", "TECH",
                LocalDateTime.of(2026, 11, 12, 10, 0),
                Set.of("java"), List.of()
        );
    }

    private String validCreateJson() {
        return """
                {
                  "advertiserId": 10,
                  "title": "Formation Java",
                  "description": "Description",
                  "landingUrl": "https://example.com",
                  "niche": "TECH",
                  "tags": ["java"]
                }
                """;
    }

    private String validUpdateJson() {
        return """
                {
                  "title": "Formation Spring",
                  "description": "Nouvelle description",
                  "landingUrl": "https://spring.io",
                  "niche": "TECH",
                  "tags": ["spring"]
                }
                """;
    }

    @Test
    void shouldReturn201WhenCreatingOffer() throws Exception {
        when(offerService.create(any(CreateOfferRequest.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.advertiserId").value(10))
                .andExpect(jsonPath("$.title").value("Formation Java"));

        verify(offerService).create(any(CreateOfferRequest.class));
    }

    @Test
    void shouldReturn400ForInvalidOffer() throws Exception {
        mockMvc.perform(post("/api/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "advertiserId": -1,
                                  "title": "",
                                  "landingUrl": "",
                                  "niche": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        verify(offerService, never()).create(any());
    }

    @Test
    void shouldReturn409ForInactiveAdvertiser() throws Exception {
        when(offerService.create(any(CreateOfferRequest.class)))
                .thenThrow(new BusinessConflictException(
                        "Advertiser does not exist or is not ACTIVE"));

        mockMvc.perform(post("/api/offres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void shouldReturnOfferById() throws Exception {
        when(offerService.getById(1L)).thenReturn(response());

        mockMvc.perform(get("/api/offres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Formation Java"));
    }

    @Test
    void shouldReturn404ForUnknownOffer() throws Exception {
        when(offerService.getById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Offer not found with id: 99"));

        mockMvc.perform(get("/api/offres/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail")
                        .value("Offer not found with id: 99"));
    }

    @Test
    void shouldSearchOffersWithPagination() throws Exception {
        when(offerService.search(
                any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response())));

        mockMvc.perform(get("/api/offres")
                        .param("niche", "TECH")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Formation Java"));
    }

    @Test
    void shouldReturn400ForInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/offres")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(offerService);
    }

    @Test
    void shouldReturn400ForInvalidPageSize() throws Exception {
        mockMvc.perform(get("/api/offres")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(offerService);
    }

    @Test
    void shouldReturn400ForUnsupportedSort() throws Exception {
        mockMvc.perform(get("/api/offres")
                        .param("sort", "password,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").isNotEmpty());

        verifyNoInteractions(offerService);
    }

    @Test
    void shouldUpdateOffer() throws Exception {
        when(offerService.update(eq(1L),
                any(UpdateOfferRequest.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/offres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(offerService).update(eq(1L),
                any(UpdateOfferRequest.class));
    }

    @Test
    void shouldReturn400ForInvalidUpdate() throws Exception {
        mockMvc.perform(put("/api/offres/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "landingUrl": "",
                                  "niche": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(offerService, never()).update(anyLong(), any());
    }

    @Test
    void shouldReturn404WhenUpdatingUnknownOffer() throws Exception {
        when(offerService.update(eq(99L), any(UpdateOfferRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Offer not found with id: 99"));

        mockMvc.perform(put("/api/offres/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateJson()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
