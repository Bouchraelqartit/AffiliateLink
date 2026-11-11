package com.affiliateLink.profile.controller;

import com.affiliateLink.common.EmailAlreadyExistsException;
import com.affiliateLink.profile.dto.AdvertiserRequest;
import com.affiliateLink.profile.dto.AdvertiserResponse;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvertiserController.class)
class AdvertiserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void shouldCreateAdvertiserAndReturn201() throws Exception {
        AdvertiserResponse response = new AdvertiserResponse(
                1L,
                "contact@company.com",
                ProfileStatus.PENDING,
                LocalDateTime.of(2026, 11, 11, 10, 0),
                "Tech Company",
                "Technology",
                "https://company.com"
        );
        when(profileService.createAdvertiser(any(AdvertiserRequest.class))).thenReturn(response);
        String requestBody = """
                {
                    "email": "contact@company.com",
                    "companyName": "Tech Company",
                    "sector": "Technology",
                    "website": "https://company.com"
                }
                """;


        mockMvc.perform(post("/api/advertisers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("contact@company.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.companyName").value("Tech Company"));
        verify(profileService).createAdvertiser(any(AdvertiserRequest.class));
    }

    @Test
    void shouldReturn400WhenAdvertiserEmailIsInvalid() throws Exception {
        String requestBody = """
                {
                    "email": "invalid-email",
                    "companyName": "Tech Company",
                    "sector": "Technology",
                    "website": "https://company.com"
                }
                """;


        mockMvc.perform(
                        post("/api/advertisers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
        verify(profileService, never()).createAdvertiser(any(AdvertiserRequest.class));
    }

    @Test
    void shouldReturn404WhenAdvertiserDoesNotExist() throws Exception {
        Long advertiserId = 999L;
        when(profileService.getAdvertiserById(advertiserId)).thenReturn(Optional.empty());


        mockMvc.perform(get("/api/advertisers/{id}", advertiserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Advertiser not found with id: 999"));
        verify(profileService).getAdvertiserById(advertiserId);
    }

    @Test
    void shouldReturn409WhenAdvertiserEmailAlreadyExists() throws Exception {
        String requestBody = """
                {
                    "email": "contact@company.com",
                    "companyName": "Tech Company",
                    "sector": "Technology",
                    "website": "https://company.com"
                }
                """;
        when(profileService.createAdvertiser(any(AdvertiserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("contact@company.com"));


        mockMvc.perform(post("/api/advertisers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Email already exists: contact@company.com"));
        verify(profileService).createAdvertiser(any(AdvertiserRequest.class));
    }
}
