package com.affiliateLink.profile.controller;

import com.affiliateLink.common.EmailAlreadyExistsException;
import com.affiliateLink.profile.dto.PublisherRequest;
import com.affiliateLink.profile.dto.PublisherResponse;
import com.affiliateLink.profile.entity.Niche;
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

@WebMvcTest(PublisherController.class)
class PublisherControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void shouldCreatePublisherAndReturn201() throws Exception {
        PublisherResponse response = new PublisherResponse(
                1L,
                "publisher@test.com",
                ProfileStatus.PENDING,
                LocalDateTime.of(2026, 11, 11, 10, 0),
                "Sarra",
                "Test",
                Niche.TECH,
                "Content creator",
                "Morocco"
        );
        when(profileService.createPublisher(any(PublisherRequest.class))).thenReturn(response);
        String requestBody = """
                {
                    "email": "publisher@test.com",
                    "firstName": "Sarra",
                    "lastName": "Test",
                    "niche": "TECH",
                    "bio": "Content creator",
                    "country": "Morocco"
                }
                """;


        mockMvc.perform(post("/api/publishers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("publisher@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.firstName").value("Sarra"));
        verify(profileService).createPublisher(any(PublisherRequest.class));
    }

    @Test
    void shouldReturn400WhenPublisherEmailIsInvalid() throws Exception {
        String requestBody = """
                {
                    "email": "invalid-email",
                    "firstName": "Sarra",
                    "lastName": "Test",
                    "niche": "TECH",
                    "bio": "Content creator",
                    "country": "Morocco"
                }
                """;


        mockMvc.perform(post("/api/publishers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        verify(profileService, never())
                .createPublisher(any(PublisherRequest.class));
    }

    @Test
    void shouldReturn404WhenPublisherDoesNotExist() throws Exception {
        Long publisherId = 999L;
        when(profileService.getPublisherById(publisherId)).thenReturn(Optional.empty());


        mockMvc.perform(
                        get("/api/publishers/{id}", publisherId)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail")
                        .value("Publisher not found with id: 999"));

        verify(profileService).getPublisherById(publisherId);
    }

    @Test
    void shouldReturn409WhenPublisherEmailAlreadyExists() throws Exception {
        String requestBody = """
                {
                    "email": "publisher@test.com",
                    "firstName": "Sarra",
                    "lastName": "Test",
                    "niche": "TECH",
                    "bio": "Content creator",
                    "country": "Morocco"
                }
                """;
        when(profileService.createPublisher(any(PublisherRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("publisher@test.com"));


        mockMvc.perform(post("/api/publishers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Email already exists: publisher@test.com"));
        verify(profileService)
                .createPublisher(any(PublisherRequest.class));
    }
}
