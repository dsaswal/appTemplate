package dev.dsa.config;

import dev.dsa.entity.Customer;
import dev.dsa.entity.Permission;
import dev.dsa.entity.Role;
import dev.dsa.entity.User;
import dev.dsa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing application data...");

        // Create Permissions
        Permission customerRead = createPermission("CUSTOMER_READ", "Read customer data");
        Permission customerWrite = createPermission("CUSTOMER_WRITE", "Create and update customers");
        Permission customerDelete = createPermission("CUSTOMER_DELETE", "Delete customers");
        Permission userManage = createPermission("USER_MANAGE", "Manage users");
        Permission roleManage = createPermission("ROLE_MANAGE", "Manage roles and permissions");
        Permission auditView = createPermission("AUDIT_VIEW", "View audit logs");

        log.info("Created {} permissions", permissionRepository.count());

        // Create Roles with Inheritance
        // Base role for all users
        Role userRole = createRole("USER", "Basic user role", null);

        // Customer service role - inherits from USER
        Role customerServiceRole = createRole("CUSTOMER_SERVICE", "Customer service representative", userRole);
        customerServiceRole.getPermissions().add(customerRead);
        customerServiceRole.getPermissions().add(customerWrite);
        roleRepository.save(customerServiceRole);

        // Manager role - inherits from CUSTOMER_SERVICE
        Role managerRole = createRole("MANAGER", "Manager with extended permissions", customerServiceRole);
        managerRole.getPermissions().add(customerDelete);
        managerRole.getPermissions().add(auditView);
        roleRepository.save(managerRole);

        // Admin role - inherits from MANAGER
        Role adminRole = createRole("ADMIN", "Administrator with full access", managerRole);
        adminRole.getPermissions().add(userManage);
        adminRole.getPermissions().add(roleManage);
        roleRepository.save(adminRole);

        log.info("Created {} roles", roleRepository.count());

        // Create Users
        User admin = createUser("admin", "admin123", "admin@example.com", "Admin", "User");
        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        User manager = createUser("manager", "manager123", "manager@example.com", "Manager", "User");
        manager.getRoles().add(managerRole);
        userRepository.save(manager);

        User csrep = createUser("csrep", "csrep123", "csrep@example.com", "Customer Service", "Rep");
        csrep.getRoles().add(customerServiceRole);
        userRepository.save(csrep);

        User basicUser = createUser("user", "user123", "user@example.com", "Basic", "User");
        basicUser.getRoles().add(userRole);
        userRepository.save(basicUser);

        log.info("Created {} users", userRepository.count());

        // Create Sample Customers
        createCustomer("John Doe", "john.doe@example.com", "555-0101", "123 Main St, Springfield", "admin");
        createCustomer("Jane Smith", "jane.smith@example.com", "555-0102", "456 Oak Ave, Springfield", "admin");
        createCustomer("Bob Johnson", "bob.johnson@example.com", "555-0103", "789 Pine Rd, Springfield", "admin");
        createCustomer("Alice Williams", "alice.williams@example.com", "555-0104", "321 Elm St, Springfield", "admin");
        createCustomer("Charlie Brown", "charlie.brown@example.com", "555-0105", "654 Maple Dr, Springfield", "admin");

        log.info("Created {} customers", customerRepository.count());

        log.info("=== Data Initialization Complete ===");
        log.info("Login Credentials:");
        log.info("  Admin:    username=admin, password=admin123");
        log.info("  Manager:  username=manager, password=manager123");
        log.info("  CS Rep:   username=csrep, password=csrep123");
        log.info("  User:     username=user, password=user123");
        log.info("");
        log.info("Role Inheritance:");
        log.info("  ADMIN -> MANAGER -> CUSTOMER_SERVICE -> USER");
        log.info("  (Each role inherits all permissions from its parent)");
        log.info("====================================");
    }

    private Permission createPermission(String name, String description) {
        if (permissionRepository.findByName(name).isPresent()) {
            return permissionRepository.findByName(name).get();
        }
        Permission permission = Permission.builder()
            .name(name)
            .description(description)
            .active(true)
            .build();
        return permissionRepository.save(permission);
    }

    private Role createRole(String name, String description, Role parentRole) {
        if (roleRepository.findByName(name).isPresent()) {
            return roleRepository.findByName(name).get();
        }
        Role role = Role.builder()
            .name(name)
            .description(description)
            .parentRole(parentRole)
            .active(true)
            .permissions(new HashSet<>())
            .build();
        return roleRepository.save(role);
    }

    private User createUser(String username, String password, String email, String firstName, String lastName) {
        if (userRepository.findByUsername(username).isPresent()) {
            return userRepository.findByUsername(username).get();
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
            .roles(new HashSet<>())
            .build();
        return user;
    }

    private void createCustomer(String name, String email, String phone, String address, String createdBy) {
        if (customerRepository.findByEmail(email).isPresent()) {
            return;
        }
        Customer customer = Customer.builder()
            .name(name)
            .email(email)
            .phone(phone)
            .address(address)
            .active(true)
            .createdBy(createdBy)
            .updatedBy(createdBy)
            .build();
        customerRepository.save(customer);
    }
}
