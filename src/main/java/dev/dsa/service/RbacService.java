package dev.dsa.service;

import dev.dsa.entity.Permission;
import dev.dsa.entity.Role;
import dev.dsa.repository.PermissionRepository;
import dev.dsa.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RbacService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // Permission Management
    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public Permission createPermission(String name, String description) {
        log.info("Creating permission: {}", name);
        Permission permission = Permission.builder()
            .name(name)
            .description(description)
            .active(true)
            .build();
        return permissionRepository.save(permission);
    }

    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public Permission updatePermission(Long id, String name, String description, Boolean active) {
        log.info("Updating permission: {}", id);
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + id));

        if (name != null) permission.setName(name);
        if (description != null) permission.setDescription(description);
        if (active != null) permission.setActive(active);

        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    @Cacheable("permissions")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Permission> getPermissionById(Long id) {
        return permissionRepository.findById(id);
    }

    // Role Management
    @Transactional
    @CacheEvict(value = {"roles", "permissions"}, allEntries = true)
    public Role createRole(String name, String description, Long parentRoleId) {
        log.info("Creating role: {} with parent: {}", name, parentRoleId);

        Role role = Role.builder()
            .name(name)
            .description(description)
            .active(true)
            .build();

        if (parentRoleId != null) {
            Role parentRole = roleRepository.findById(parentRoleId)
                .orElseThrow(() -> new RuntimeException("Parent role not found: " + parentRoleId));
            role.setParentRole(parentRole);
        }

        return roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = {"roles", "permissions"}, allEntries = true)
    public Role updateRole(Long id, String name, String description, Boolean active, Long parentRoleId) {
        log.info("Updating role: {}", id);
        Role role = roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        if (name != null) role.setName(name);
        if (description != null) role.setDescription(description);
        if (active != null) role.setActive(active);

        if (parentRoleId != null) {
            Role parentRole = roleRepository.findById(parentRoleId)
                .orElseThrow(() -> new RuntimeException("Parent role not found: " + parentRoleId));

            // Prevent circular inheritance
            if (isCircularInheritance(role, parentRole)) {
                throw new RuntimeException("Circular role inheritance detected");
            }
            role.setParentRole(parentRole);
        }

        return roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = {"roles", "permissions"}, allEntries = true)
    public void addPermissionToRole(Long roleId, Long permissionId) {
        log.info("Adding permission {} to role {}", permissionId, roleId);
        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));

        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    @Transactional
    @CacheEvict(value = {"roles", "permissions"}, allEntries = true)
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    @Cacheable("roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findByIdWithPermissions(id);
    }

    @Transactional(readOnly = true)
    public Set<Permission> getRoleEffectivePermissions(Long roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        return role.getAllPermissions();
    }

    private boolean isCircularInheritance(Role role, Role potentialParent) {
        Role current = potentialParent;
        while (current != null) {
            if (current.getId().equals(role.getId())) {
                return true;
            }
            current = current.getParentRole();
        }
        return false;
    }
}
