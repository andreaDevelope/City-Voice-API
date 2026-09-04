package it.cityvoice.api.features.profile.user_badge;

import it.cityvoice.api.features.profile.badge.Badge;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(
        name = "user_badges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_rome_id", "badge_id"})
)
@Data
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_rome_id", nullable = false)
    private UserRome userRome;

    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(nullable = false)
    private Instant unlockedAt;

    @Column(nullable = false)
    private boolean featured = false;
}
