package dev.dsa.api.controller;

import dev.dsa.api.dto.ApiResponse;
import dev.dsa.entity.Permission;
import dev.dsa.service.RbacService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Permission Management", description = "Admin endpoints for managing permissions")
@SecurityRequirement(name = "Bearer Authentication")
public class PermissionApiController {

    private final RbacService rbacService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        log.info("API: Getting all permissions");
        List<Permission> permissions = rbacService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Permission>> getPermissionById(@PathVariable Long id) {
        log.info("API: Getting permission by id: {}", id);
        Permission permission = rbacService.getPermissionById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("Permission", id));
        return ResponseEntity.ok(ApiResponse.success(permission));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Permission>> createPermission(
        @RequestParam String name,
        @RequestParam(required = false) String description) {

        log.info("API: Creating permission: {}", name);
        Permission created = rbacService.createPermission(name, description);
        return ResponseEntity.ok(ApiResponse.success("Permission created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Permission>> updatePermission(
        @PathVariable Long id,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) Boolean active) {

        log.info("API: Updating permission: {}", id);
        Permission updated = rbacService.updatePermission(id, name, description, active);
        return ResponseEntity.ok(ApiResponse.success("Permission updated successfully", updated));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Permission>> togglePermission(@PathVariable Long id) {
        log.info("API: Toggling permission: {}", id);
        Permission permission = rbacService.getPermissionById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("Permission", id));

        Permission updated = rbacService.updatePermission(id, null, null, !permission.getActive());
        return ResponseEntity.ok(ApiResponse.success("Permission toggled successfully", updated));
    }
}
