package com.affiliateLink.offer.repository;

import com.affiliateLink.offer.entity.Offer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {
    Optional<Offer> findByIdAndAdvertiserId(Long id, Long advertiserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offer o where o.id = :id")
    Optional<Offer> findByIdForUpdate(@Param("id") Long id);
    List<Offer> findAllByAdvertiserId(Long advertiserId);
}
