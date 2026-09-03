package it.cityvoice.api.features.profile.badge;

import it.cityvoice.api.features.profile.category.Category;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "badges")
@Data
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int missionThreshold;

    @Column(nullable = false)
    private int sequenceOrder;
}
