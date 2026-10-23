
package com.affiliateLink.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_publishers")
@PrimaryKeyJoinColumn(name = "id")
public class Publisher extends Profile {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Niche niche;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    private String country;

    protected Publisher() {
    }
}
