package dev.dsa.api.controller;

import dev.dsa.api.dto.ApiResponse;
import dev.dsa.entity.Permission;
import dev.dsa.entity.Role;
import dev.dsa.service.RbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class RoleApiController {

    private final RbacService rbacService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        log.info("API: Getting all roles");
        List<Role> roles = rbacService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        log.info("API: Getting role by id: {}", id);
        Role role = rbacService.getRoleById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("Role", id));
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @GetMapping("/{id}/permissions/effective")
    public ResponseEntity<ApiResponse<Set<Permission>>> getEffectivePermissions(@PathVariable Long id) {
        log.info("API: Getting effective permissions for role: {}", id);
        Set<Permission> permissions = rbacService.getRoleEffectivePermissions(id);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(
        @RequestParam String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Long parentRoleId) {

        log.info("API: Creating role: {}", name);
        Role created = rbacService.createRole(name, description, parentRoleId);
        return ResponseEntity.ok(ApiResponse.success("Role created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(
        @PathVariable Long id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) Long parentRoleId) {

        log.info("API: Updating role: {}", id);
        Role updated = rbacService.updateRole(id, name, description, active, parentRoleId);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", updated));
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<Void>> addPermissionToRole(
        @PathVariable Long roleId,
        @PathVariable Long permissionId) {

        log.info("API: Adding permission {} to role {}", permissionId, roleId);
        rbacService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission added to role successfully", null));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<Void>> removePermissionFromRole(
        @PathVariable Long roleId,
        @PathVariable Long permissionId) {

        log.info("API: Removing permission {} from role {}", permissionId, roleId);
        rbacService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role successfully", null));
    }
}
