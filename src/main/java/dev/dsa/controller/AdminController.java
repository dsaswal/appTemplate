package dev.dsa.controller;

import dev.dsa.entity.Permission;
import dev.dsa.entity.Role;
import dev.dsa.entity.User;
import dev.dsa.service.AuditService;
import dev.dsa.service.RbacService;
import dev.dsa.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RbacService rbacService;
    private final AuditService auditService;

    // User Management
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", rbacService.getAllRoles());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", rbacService.getAllRoles());
        return "admin/user-form";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam(required = false) String firstName,
                           @RequestParam(required = false) String lastName,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, password, email, firstName, lastName);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (Exception e) {
            log.error("Error creating user", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", rbacService.getAllRoles());
        return "admin/user-edit";
    }

    @PostMapping("/users/{id}")
    public String updateUser(@PathVariable Long id,
                           @RequestParam String email,
                           @RequestParam(required = false) String firstName,
                           @RequestParam(required = false) String lastName,
                           @RequestParam(defaultValue = "false") Boolean enabled,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, email, firstName, lastName, enabled);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (Exception e) {
            log.error("Error updating user", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/roles/{roleId}/add")
    public String addRoleToUser(@PathVariable Long userId, @PathVariable Long roleId, RedirectAttributes redirectAttributes) {
        try {
            userService.addRoleToUser(userId, roleId);
            redirectAttributes.addFlashAttribute("success", "Role added to user");
        } catch (Exception e) {
            log.error("Error adding role to user", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + userId + "/edit";
    }

    @PostMapping("/users/{userId}/roles/{roleId}/remove")
    public String removeRoleFromUser(@PathVariable Long userId, @PathVariable Long roleId, RedirectAttributes redirectAttributes) {
        try {
            userService.removeRoleFromUser(userId, roleId);
            redirectAttributes.addFlashAttribute("success", "Role removed from user");
        } catch (Exception e) {
            log.error("Error removing role from user", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users/" + userId + "/edit";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting user", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Role Management
    @GetMapping("/roles")
    public String listRoles(Model model) {
        model.addAttribute("roles", rbacService.getAllRoles());
        model.addAttribute("permissions", rbacService.getAllPermissions());
        return "admin/roles";
    }

    @GetMapping("/roles/new")
    public String newRoleForm(Model model) {
        model.addAttribute("role", new Role());
        model.addAttribute("allRoles", rbacService.getAllRoles());
        return "admin/role-form";
    }

    @PostMapping("/roles")
    public String createRole(@RequestParam String name,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) Long parentRoleId,
                           RedirectAttributes redirectAttributes) {
        try {
            rbacService.createRole(name, description, parentRoleId);
            redirectAttributes.addFlashAttribute("success", "Role created successfully");
        } catch (Exception e) {
            log.error("Error creating role", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @GetMapping("/roles/{id}/edit")
    public String editRoleForm(@PathVariable Long id, Model model) {
        Role role = rbacService.getRoleById(id)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        model.addAttribute("role", role);
        model.addAttribute("allRoles", rbacService.getAllRoles());
        model.addAttribute("allPermissions", rbacService.getAllPermissions());
        model.addAttribute("effectivePermissions", rbacService.getRoleEffectivePermissions(id));
        return "admin/role-edit";
    }

    @PostMapping("/roles/{roleId}/permissions/add")
    public String addPermissionToRole(@PathVariable Long roleId,
                                     @RequestParam Long permissionId,
                                     RedirectAttributes redirectAttributes) {
        try {
            rbacService.addPermissionToRole(roleId, permissionId);
            redirectAttributes.addFlashAttribute("success", "Permission added to role");
        } catch (Exception e) {
            log.error("Error adding permission to role", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/" + roleId + "/edit";
    }

    @PostMapping("/roles/{roleId}/permissions/remove")
    public String removePermissionFromRole(@PathVariable Long roleId,
                                          @RequestParam Long permissionId,
                                          RedirectAttributes redirectAttributes) {
        try {
            rbacService.removePermissionFromRole(roleId, permissionId);
            redirectAttributes.addFlashAttribute("success", "Permission removed from role");
        } catch (Exception e) {
            log.error("Error removing permission from role", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/roles/" + roleId + "/edit";
    }

    // Permission Management
    @GetMapping("/permissions")
    public String listPermissions(Model model) {
        model.addAttribute("permissions", rbacService.getAllPermissions());
        return "admin/permissions";
    }

    @PostMapping("/permissions")
    public String createPermission(@RequestParam String name,
                                  @RequestParam(required = false) String description,
                                  RedirectAttributes redirectAttributes) {
        try {
            rbacService.createPermission(name, description);
            redirectAttributes.addFlashAttribute("success", "Permission created successfully");
        } catch (Exception e) {
            log.error("Error creating permission", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/permissions";
    }

    @PostMapping("/permissions/{id}/toggle")
    public String togglePermission(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Permission permission = rbacService.getPermissionById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
            rbacService.updatePermission(id, null, null, !permission.getActive());
            redirectAttributes.addFlashAttribute("success", "Permission status updated");
        } catch (Exception e) {
            log.error("Error toggling permission", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/permissions";
    }

    // Audit Logs
    @GetMapping("/audit")
    public String auditLogs(Model model) {
        model.addAttribute("logs", auditService.getRecentLogs(100));
        return "admin/audit";
    }

    @GetMapping("/audit/entity")
    public String entityAudit(@RequestParam String entityType,
                             @RequestParam Long entityId,
                             Model model) {
        model.addAttribute("entityType", entityType);
        model.addAttribute("entityId", entityId);
        model.addAttribute("logs", auditService.getLogsByEntity(entityType, entityId));
        return "admin/entity-audit";
    }
}
