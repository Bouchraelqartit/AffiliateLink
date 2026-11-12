package com.affiliateLink.offer.service;

import com.affiliateLink.common.BusinessConflictException;
import com.affiliateLink.common.ResourceNotFoundException;
import com.affiliateLink.offer.dto.CreateOfferRequest;
import com.affiliateLink.offer.dto.OfferResponse;
import com.affiliateLink.offer.dto.UpdateOfferRequest;
import com.affiliateLink.offer.entity.Offer;
import com.affiliateLink.offer.entity.Tag;
import com.affiliateLink.offer.mapper.OfferMapper;
import com.affiliateLink.offer.repository.OfferRepository;
import com.affiliateLink.offer.repository.TagRepository;
import com.affiliateLink.profile.api.ProfileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock OfferRepository offerRepository;
    @Mock TagRepository tagRepository;
    @Mock OfferMapper offerMapper;
    @Mock ProfileApi profileApi;

    @InjectMocks OfferService service;

    private CreateOfferRequest createRequest(Set<String> tags) {
        return new CreateOfferRequest(
                10L, "Formation Java", "Description",
                "https://example.com", " TECH ", tags);
    }

    @Test
    void shouldCreateOfferForActiveAdvertiser() {
        when(profileApi.isAdvertiserActive(10L)).thenReturn(true);
        when(offerRepository.save(any(Offer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(createRequest(Set.of()));

        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(captor.capture());

        Offer saved = captor.getValue();
        assertThat(saved.getAdvertiserId()).isEqualTo(10L);
        assertThat(saved.getTitle()).isEqualTo("Formation Java");
        assertThat(saved.getNiche()).isEqualTo("TECH");
    }

    @Test
    void shouldRejectOfferWhenAdvertiserInactiveOrMissing() {
        when(profileApi.isAdvertiserActive(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(createRequest(Set.of())))
                .isInstanceOf(BusinessConflictException.class);

        verify(offerRepository, never()).save(any());
    }

    @Test
    void shouldNormalizeAndReuseExistingTags() {
        when(profileApi.isAdvertiserActive(10L)).thenReturn(true);
        Tag existing = new Tag("tech");
        when(tagRepository.findByName("tech"))
                .thenReturn(Optional.of(existing));
        when(offerRepository.save(any(Offer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(createRequest(Set.of("  TeCH  ")));

        ArgumentCaptor<Offer> captor = ArgumentCaptor.forClass(Offer.class);
        verify(offerRepository).save(captor.capture());

        assertThat(captor.getValue().getTags()).containsExactly(existing);
        verify(tagRepository, never()).save(any());
    }

    @Test
    void shouldCreateNewTagWhenMissing() {
        when(profileApi.isAdvertiserActive(10L)).thenReturn(true);
        when(tagRepository.findByName("spring")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(offerRepository.save(any(Offer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(createRequest(Set.of(" SPRING ")));

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getName()).isEqualTo("spring");
    }

    @Test
    void shouldRejectMissingOfferOnGetById() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetOfferById() {
        Offer offer = new Offer();
        OfferResponse response = mock(OfferResponse.class);

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerMapper.toResponse(offer)).thenReturn(response);

        assertThat(service.getById(1L)).isSameAs(response);
    }

    @Test
    void shouldUpdateExistingOffer() {
        Offer offer = new Offer();
        offer.setAdvertiserId(10L);
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UpdateOfferRequest request = new UpdateOfferRequest(
                "Nouveau titre", "Nouvelle description",
                "https://new.com", " GAMING ", Set.of());

        service.update(1L, request);

        assertThat(offer.getTitle()).isEqualTo("Nouveau titre");
        assertThat(offer.getNiche()).isEqualTo("GAMING");
        verify(offerRepository).save(offer);
    }

    @Test
    void shouldRejectUpdateOfUnknownOffer() {
        when(offerRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateOfferRequest request = new UpdateOfferRequest(
                "Test", "Description", "https://example.com",
                "TECH", Set.of());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(offerRepository, never()).save(any());
    }

    @Test
    void shouldSearchOffersWithPagination() {
        Offer offer = new Offer();
        Pageable pageable = PageRequest.of(0, 5);
        OfferResponse response = mock(OfferResponse.class);

        when(offerRepository.findAll(
                any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(offer), pageable, 1));
        when(offerMapper.toResponse(offer)).thenReturn(response);

        Page<OfferResponse> result =
                service.search("TECH", 10L, "java", "formation", pageable);

        assertThat(result.getContent()).containsExactly(response);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(offerRepository).findAll(
                any(Specification.class), eq(pageable));
    }

    @Test
    void shouldSearchWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        when(offerRepository.findAll(
                any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        Page<OfferResponse> result =
                service.search(null, null, null, null, pageable);

        assertThat(result).isEmpty();
    }
}
