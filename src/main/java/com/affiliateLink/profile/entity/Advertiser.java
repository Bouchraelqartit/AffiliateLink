
package com.affiliateLink.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_advertisers")
@PrimaryKeyJoinColumn(name = "id")
public class Advertiser extends Profile {

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private String website;

    protected Advertiser() {
    }
}
