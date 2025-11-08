package dev.dsa.api.controller;

import dev.dsa.api.dto.ApiResponse;
import dev.dsa.entity.User;
import dev.dsa.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserApiController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("API: Getting all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("API: Getting user by id: {}", id);
        User user = userService.getUserById(id)
            .orElseThrow(() -> new dev.dsa.exception.ResourceNotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String email,
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName) {

        log.info("API: Creating user: {}", username);
        User created = userService.createUser(username, password, email, firstName, lastName);
        return ResponseEntity.ok(ApiResponse.success("User created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
        @PathVariable Long id,
        @RequestParam String email,
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(defaultValue = "false") Boolean enabled) {

        log.info("API: Updating user: {}", id);
        User updated = userService.updateUser(id, email, firstName, lastName, enabled);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> addRoleToUser(
        @PathVariable Long userId,
        @PathVariable Long roleId) {

        log.info("API: Adding role {} to user {}", roleId, userId);
        userService.addRoleToUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role added to user successfully", null));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
        @PathVariable Long userId,
        @PathVariable Long roleId) {

        log.info("API: Removing role {} from user {}", roleId, userId);
        userService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed from user successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("API: Deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
