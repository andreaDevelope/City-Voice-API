package it.cityvoice.api.features.stories.entity;

import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.enums.StoryStatus;
import it.cityvoice.api.features.stories.enums.StoryType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stories")
@Data
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_rome_id", nullable = false)
    private UserRome userRome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StoryType type;

    @Column
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String storyContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryStatus status = StoryStatus.PUBLISHED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}