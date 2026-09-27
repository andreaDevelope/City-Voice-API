package it.cityvoice.api.features.districts.entity;

import it.cityvoice.api.features.districts.enums.DistrictType;
import it.cityvoice.api.features.districts.enums.Municipio;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "districts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name"}),
        indexes = @Index(name = "idx_district_municipio", columnList = "municipio")
)
@Data
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Municipio municipio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistrictType type;
}