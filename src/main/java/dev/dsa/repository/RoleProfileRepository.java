package dev.dsa.repository;

import dev.dsa.entity.RoleProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleProfileRepository extends JpaRepository<RoleProfile, Long> {

    Optional<RoleProfile> findByName(String name);

    List<RoleProfile> findByActive(Boolean active);

    @Query("SELECT rp FROM RoleProfile rp WHERE rp.name LIKE %:keyword% OR rp.description LIKE %:keyword%")
    List<RoleProfile> searchByKeyword(String keyword);

    boolean existsByName(String name);
}
