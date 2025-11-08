package dev.dsa.service;

import dev.dsa.entity.Role;
import dev.dsa.entity.RoleProfile;
import dev.dsa.exception.ResourceNotFoundException;
import dev.dsa.repository.RoleProfileRepository;
import dev.dsa.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleProfileService {

    private final RoleProfileRepository roleProfileRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Transactional
    public RoleProfile createProfile(RoleProfile profile, Set<Long> roleIds) {
        log.info("Creating role profile: {}", profile.getName());

        // Check if profile name already exists
        if (roleProfileRepository.existsByName(profile.getName())) {
            throw new IllegalArgumentException("Profile with name '" + profile.getName() + "' already exists");
        }

        // Load and add roles
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
                roles.add(role);
            }
            profile.setRoles(roles);
        }

        RoleProfile savedProfile = roleProfileRepository.save(profile);

        auditService.logAction("CREATE", "RoleProfile", savedProfile.getId(),
            "Created role profile: " + savedProfile.getName(), null, savedProfile.toString());

        return savedProfile;
    }

    @Transactional
    public RoleProfile updateProfile(Long id, RoleProfile profileDetails, Set<Long> roleIds) {
        log.info("Updating role profile: {}", id);

        RoleProfile profile = roleProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RoleProfile", id));

        String oldValue = profile.toString();

        // Check if new name conflicts with existing profile
        if (!profile.getName().equals(profileDetails.getName()) &&
            roleProfileRepository.existsByName(profileDetails.getName())) {
            throw new IllegalArgumentException("Profile with name '" + profileDetails.getName() + "' already exists");
        }

        profile.setName(profileDetails.getName());
        profile.setDescription(profileDetails.getDescription());
        profile.setActive(profileDetails.getActive());

        // Update roles
        if (roleIds != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
                roles.add(role);
            }
            profile.setRoles(roles);
        }

        RoleProfile updatedProfile = roleProfileRepository.save(profile);

        auditService.logAction("UPDATE", "RoleProfile", updatedProfile.getId(),
            "Updated role profile: " + updatedProfile.getName(), oldValue, updatedProfile.toString());

        return updatedProfile;
    }

    @Transactional
    public void deleteProfile(Long id) {
        log.info("Deleting role profile: {}", id);

        RoleProfile profile = roleProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RoleProfile", id));

        String profileName = profile.getName();
        roleProfileRepository.delete(profile);

        auditService.logAction("DELETE", "RoleProfile", id,
            "Deleted role profile: " + profileName, profile.toString(), null);
    }

    @Transactional(readOnly = true)
    public List<RoleProfile> getAllProfiles() {
        return roleProfileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RoleProfile> getActiveProfiles() {
        return roleProfileRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public Optional<RoleProfile> getProfileById(Long id) {
        return roleProfileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RoleProfile> getProfileByName(String name) {
        return roleProfileRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<RoleProfile> searchProfiles(String keyword) {
        return roleProfileRepository.searchByKeyword(keyword);
    }

    @Transactional
    public RoleProfile addRoleToProfile(Long profileId, Long roleId) {
        log.info("Adding role {} to profile {}", roleId, profileId);

        RoleProfile profile = roleProfileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("RoleProfile", profileId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        profile.addRole(role);
        RoleProfile updatedProfile = roleProfileRepository.save(profile);

        auditService.logAction("UPDATE", "RoleProfile", profileId,
            "Added role " + role.getName() + " to profile " + profile.getName(), null, null);

        return updatedProfile;
    }

    @Transactional
    public RoleProfile removeRoleFromProfile(Long profileId, Long roleId) {
        log.info("Removing role {} from profile {}", roleId, profileId);

        RoleProfile profile = roleProfileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("RoleProfile", profileId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        profile.removeRole(role);
        RoleProfile updatedProfile = roleProfileRepository.save(profile);

        auditService.logAction("UPDATE", "RoleProfile", profileId,
            "Removed role " + role.getName() + " from profile " + profile.getName(), null, null);

        return updatedProfile;
    }
}
