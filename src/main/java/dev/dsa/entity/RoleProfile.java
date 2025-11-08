package dev.dsa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * RoleProfile allows composing multiple roles into a single profile
 * This enables flexible role assignment without multiple inheritance complexity
 *
 * Example profiles:
 * - "Sales Manager" = [MANAGER_ROLE, SALES_ROLE, CRM_ACCESS]
 * - "Regional Director" = [DIRECTOR_ROLE, MULTI_REGION_ACCESS, BUDGET_APPROVAL]
 */
@Entity
@Table(name = "role_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "profile_roles",
        joinColumns = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get all permissions from all roles in this profile
     */
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>();
        for (Role role : roles) {
            allPermissions.addAll(role.getAllPermissions());
        }
        return allPermissions;
    }

    /**
     * Add a role to this profile
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Remove a role from this profile
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
}
