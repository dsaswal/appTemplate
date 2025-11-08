package dev.dsa.api.controller;

import dev.dsa.api.dto.ApiResponse;
import dev.dsa.entity.Permission;
import dev.dsa.entity.RoleProfile;
import dev.dsa.service.RoleProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/profiles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Role Profile Management", description = "Admin endpoints for managing role profiles and profile-role assignments")
@SecurityRequirement(name = "Bearer Authentication")
public class RoleProfileApiController {

    private final RoleProfileService roleProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleProfile>>> getAllProfiles() {
        log.info("API: Getting all role profiles");
        List<RoleProfile> profiles = roleProfileService.getAllProfiles();
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RoleProfile>>> getActiveProfiles() {
        log.info("API: Getting active role profiles");
        List<RoleProfile> profiles = roleProfileService.getActiveProfiles();
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleProfile>> getProfileById(@PathVariable Long id) {
        log.info("API: Getting role profile by id: {}", id);
        RoleProfile profile = roleProfileService.getProfileById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("RoleProfile", id));
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<Set<Permission>>> getProfilePermissions(@PathVariable Long id) {
        log.info("API: Getting permissions for role profile: {}", id);
        RoleProfile profile = roleProfileService.getProfileById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("RoleProfile", id));
        Set<Permission> permissions = profile.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RoleProfile>>> searchProfiles(@RequestParam String keyword) {
        log.info("API: Searching role profiles with keyword: {}", keyword);
        List<RoleProfile> profiles = roleProfileService.searchProfiles(keyword);
        return ResponseEntity.ok(ApiResponse.success(profiles));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleProfile>> createProfile(
        @RequestParam String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Set<Long> roleIds) {

        log.info("API: Creating role profile: {}", name);

        RoleProfile profile = RoleProfile.builder()
            .name(name)
            .description(description)
            .active(true)
            .build();

        RoleProfile created = roleProfileService.createProfile(profile, roleIds);
        return ResponseEntity.ok(ApiResponse.success("Role profile created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleProfile>> updateProfile(
        @PathVariable Long id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) Set<Long> roleIds) {

        log.info("API: Updating role profile: {}", id);

        RoleProfile profile = roleProfileService.getProfileById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("RoleProfile", id));

        RoleProfile profileDetails = RoleProfile.builder()
            .name(name != null ? name : profile.getName())
            .description(description != null ? description : profile.getDescription())
            .active(active != null ? active : profile.getActive())
            .build();

        RoleProfile updated = roleProfileService.updateProfile(id, profileDetails, roleIds);
        return ResponseEntity.ok(ApiResponse.success("Role profile updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable Long id) {
        log.info("API: Deleting role profile: {}", id);
        roleProfileService.deleteProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Role profile deleted successfully", null));
    }

    @PostMapping("/{profileId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<RoleProfile>> addRoleToProfile(
        @PathVariable Long profileId,
        @PathVariable Long roleId) {

        log.info("API: Adding role {} to profile {}", roleId, profileId);
        RoleProfile updated = roleProfileService.addRoleToProfile(profileId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role added to profile successfully", updated));
    }

    @DeleteMapping("/{profileId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<RoleProfile>> removeRoleFromProfile(
        @PathVariable Long profileId,
        @PathVariable Long roleId) {

        log.info("API: Removing role {} from profile {}", roleId, profileId);
        RoleProfile updated = roleProfileService.removeRoleFromProfile(profileId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed from profile successfully", updated));
    }
}
