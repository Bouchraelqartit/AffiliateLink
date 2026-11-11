package com.affiliateLink.profile.controller;

import com.affiliateLink.common.InvalidStatusTransitionException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProfileController.class)
class AdminProfileControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void shouldUpdateProfileStatusAndReturn204() throws Exception {
        Long profileId = 1L;
        doNothing().when(profileService).updateProfileStatus(profileId, ProfileStatus.ACTIVE);
        String requestBody = """
                {
                    "status": "ACTIVE"
                }
                """;


        mockMvc.perform(patch("/api/admin/profiles/{id}/status", profileId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isNoContent());
        verify(profileService).updateProfileStatus(profileId, ProfileStatus.ACTIVE);
    }

    @Test
    void shouldReturn400WhenStatusIsMissing() throws Exception {
        String requestBody = "{}";


        mockMvc.perform(patch("/api/admin/profiles/{id}/status", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
        verify(profileService, never()).updateProfileStatus(anyLong(), any(ProfileStatus.class));
    }

    @Test
    void shouldReturn404WhenProfileDoesNotExist() throws Exception {
        Long profileId = 999L;
        doThrow(new ResourceNotFoundException("Profile not found with id: " + profileId))
                .when(profileService)
                .updateProfileStatus(profileId, ProfileStatus.ACTIVE);
        String requestBody = """
                {
                    "status": "ACTIVE"
                }
                """;


        mockMvc.perform(patch("/api/admin/profiles/{id}/status", profileId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Profile not found with id: 999"));
        verify(profileService).updateProfileStatus(profileId, ProfileStatus.ACTIVE);
    }

    @Test
    void shouldReturn409WhenStatusTransitionIsInvalid() throws Exception {
        Long profileId = 1L;
        doThrow(new InvalidStatusTransitionException("Invalid status transition"))
                .when(profileService)
                .updateProfileStatus(profileId, ProfileStatus.PENDING);
        String requestBody = """
                {
                    "status": "PENDING"
                }
                """;


        mockMvc.perform(patch("/api/admin/profiles/{id}/status", profileId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
        verify(profileService).updateProfileStatus(profileId, ProfileStatus.PENDING);
    }
}
