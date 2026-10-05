package it.cityvoice.api.features.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import it.cityvoice.api.features.auth.enums.Role;

import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String recoveryKeyHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "app_user_roles",
            joinColumns = @JoinColumn(name = "app_user_id"),
            uniqueConstraints = @UniqueConstraint(name = "uq_app_user_role", columnNames = {"app_user_id", "roles"})
    )
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

}
