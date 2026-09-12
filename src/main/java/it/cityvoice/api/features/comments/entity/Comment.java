package it.cityvoice.api.features.comments.entity;

import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.stories.entity.Story;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_rome_id", nullable = false)
    private UserRome userRome;

    @ManyToOne
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int appliedDelta;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}