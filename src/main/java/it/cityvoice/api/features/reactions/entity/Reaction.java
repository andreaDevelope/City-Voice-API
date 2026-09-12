package it.cityvoice.api.features.reactions.entity;

import it.cityvoice.api.features.comments.entity.Comment;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.reactions.enums.ReactionType;
import it.cityvoice.api.features.stories.entity.Story;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity

 // il vincolo @UniqueConstraint sotto non basta da solo
 // La protezione reale è un indice unico parziale
 // definito manualmente in schema.sql (non generabile da Hibernate/JPA). Non rimuovere questo constraint.
 // Dettagli: docs/00-stack-and-architecture.md#reaction-uniqueness

@Table(
        name = "reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_rome_id", "story_id", "comment_id"}),
        indexes = {
                @Index(name = "idx_reaction_story", columnList = "story_id"),
                @Index(name = "idx_reaction_comment", columnList = "comment_id")
        }
)
@Data
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne
    @JoinColumn(name = "user_rome_id", nullable = false)
    private UserRome userRome;

    // story e comment: esattamente uno dei due deve essere valorizzato (mai entrambi, mai nessuno)
    // Dettagli: docs/00-stack-and-architecture.md#reaction-uniqueness
    @ManyToOne
    @JoinColumn(name = "story_id")
    private Story story;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @Column(nullable = false)
    private int appliedDelta;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}