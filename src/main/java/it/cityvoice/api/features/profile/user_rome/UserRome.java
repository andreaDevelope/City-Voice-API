package it.cityvoice.api.features.profile.user_rome;

import it.cityvoice.api.features.auth.entity.AppUser;
import jakarta.persistence.*;
import lombok.Data;


    @Entity
    @Table(name = "users_rome")
    @Data
    public class UserRome {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private Long id;

        @OneToOne
        @JoinColumn(name = "app_user_id", nullable = false, unique = true)
        private AppUser appUser;

        private String symbol;

        private String color;

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



