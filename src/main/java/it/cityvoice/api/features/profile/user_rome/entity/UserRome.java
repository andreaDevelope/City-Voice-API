package it.cityvoice.api.features.profile.user_rome.entity;

import it.cityvoice.api.features.auth.entity.AppUser;
import it.cityvoice.api.features.profile.enums.ProfileColor;
import it.cityvoice.api.features.profile.enums.ProfileSymbol;
import jakarta.persistence.*;
import lombok.Data;


    @Entity
    @Table(name = "users_rome")
    @Data
    public class UserRome {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne
        @JoinColumn(name = "app_user_id", nullable = false, unique = true)
        private AppUser appUser;

        @Enumerated(EnumType.STRING)
        private ProfileSymbol symbol;

        @Enumerated(EnumType.STRING)
        private ProfileColor color;

        private String neighborhood;

        @Column(nullable = false)
        private int activityCounter = 0;

        @Column(nullable = false)
        private int neighborhoodCounter = 0;

        @Column(nullable = false)
        private int continuityCounter = 0;

        @Column(nullable = false)
        private int impactCounter = 0;
    }



