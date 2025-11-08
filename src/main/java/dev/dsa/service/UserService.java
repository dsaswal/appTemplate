package dev.dsa.service;

import dev.dsa.entity.Role;
import dev.dsa.entity.User;
import dev.dsa.repository.RoleRepository;
import dev.dsa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional
    public User createUser(String username, String password, String email, String firstName, String lastName) {
        log.info("Creating user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

        User savedUser = userRepository.save(user);
        auditService.logAction("CREATE", "User", savedUser.getId(), "Created user: " + username, null, username);

        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, String email, String firstName, String lastName, Boolean enabled) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));

        String oldValue = String.format("Email: %s, Name: %s %s, Enabled: %s",
            user.getEmail(), user.getFirstName(), user.getLastName(), user.getEnabled());

        if (email != null) user.setEmail(email);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (enabled != null) user.setEnabled(enabled);

        User updatedUser = userRepository.save(user);

        String newValue = String.format("Email: %s, Name: %s %s, Enabled: %s",
            updatedUser.getEmail(), updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getEnabled());

        auditService.logAction("UPDATE", "User", updatedUser.getId(),
            "Updated user: " + user.getUsername(), oldValue, newValue);

        return updatedUser;
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditService.logAction("UPDATE", "User", userId, "Password changed for user: " + user.getUsername(), null, null);
    }

    @Transactional
    public void addRoleToUser(Long userId, Long roleId) {
        log.info("Adding role {} to user {}", roleId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        user.getRoles().add(role);
        userRepository.save(user);

        auditService.logAction("UPDATE", "User", userId,
            "Added role " + role.getName() + " to user " + user.getUsername(), null, null);
    }

    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.getRoles().removeIf(r -> r.getId().equals(roleId));
        userRepository.save(user);

        auditService.logAction("UPDATE", "User", userId,
            "Removed role from user " + user.getUsername(), null, null);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String username = user.getUsername();
        userRepository.delete(user);

        auditService.logAction("DELETE", "User", userId, "Deleted user: " + username, username, null);
    }

    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
