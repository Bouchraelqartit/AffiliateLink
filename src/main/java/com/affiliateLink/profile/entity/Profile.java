
package com.affiliateLink.profile.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.affiliateLink.common.InvalidStatusTransitionException;

@Entity
@Table(name = "profile_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileStatus status = ProfileStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Profile() {
    }

    protected Profile(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public ProfileStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void changeStatus(ProfileStatus newStatus) {

        boolean validTransition = switch (this.status) {
            case PENDING -> newStatus == ProfileStatus.ACTIVE;
            case ACTIVE -> newStatus == ProfileStatus.SUSPENDED;
            case SUSPENDED -> newStatus == ProfileStatus.ACTIVE;
        };

        if (!validTransition) {
            throw new InvalidStatusTransitionException(
                    "Cannot change status from " + this.status + " to " + newStatus
            );
        }

        this.status = newStatus;
    }
}
