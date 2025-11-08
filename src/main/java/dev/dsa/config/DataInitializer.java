package dev.dsa.config;

import dev.dsa.entity.Account;
import dev.dsa.entity.Customer;
import dev.dsa.entity.Permission;
import dev.dsa.entity.Role;
import dev.dsa.entity.RoleProfile;
import dev.dsa.entity.User;
import dev.dsa.repository.*;
import dev.dsa.service.UserProfileService;
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
    private final AccountRepository accountRepository;
    private final RoleProfileRepository roleProfileRepository;
    private final UserProfileService userProfileService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing application data...");

        // Create Permissions
        Permission customerRead = createPermission("CUSTOMER_READ", "Read customer data");
        Permission customerWrite = createPermission("CUSTOMER_WRITE", "Create and update customers");
        Permission customerDelete = createPermission("CUSTOMER_DELETE", "Delete customers");
        Permission accountRead = createPermission("ACCOUNT_READ", "Read account data");
        Permission accountWrite = createPermission("ACCOUNT_WRITE", "Create and update accounts");
        Permission accountDelete = createPermission("ACCOUNT_DELETE", "Delete accounts");
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
        customerServiceRole.getPermissions().add(accountRead);
        customerServiceRole.getPermissions().add(accountWrite);
        roleRepository.save(customerServiceRole);

        // Manager role - inherits from CUSTOMER_SERVICE
        Role managerRole = createRole("MANAGER", "Manager with extended permissions", customerServiceRole);
        managerRole.getPermissions().add(customerDelete);
        managerRole.getPermissions().add(accountDelete);
        managerRole.getPermissions().add(auditView);
        roleRepository.save(managerRole);

        // Admin role - inherits from MANAGER
        Role adminRole = createRole("ADMIN", "Administrator with full access", managerRole);
        adminRole.getPermissions().add(userManage);
        adminRole.getPermissions().add(roleManage);
        roleRepository.save(adminRole);

        log.info("Created {} roles", roleRepository.count());

        // Create Role Profiles (combining multiple roles)
        RoleProfile salesManagerProfile = createRoleProfile(
            "SALES_MANAGER",
            "Sales Manager with customer and account management",
            Set.of(managerRole, customerServiceRole)
        );

        RoleProfile customerSupportProfile = createRoleProfile(
            "CUSTOMER_SUPPORT",
            "Customer support with read access",
            Set.of(customerServiceRole, userRole)
        );

        RoleProfile dataAnalystProfile = createRoleProfile(
            "DATA_ANALYST",
            "Data analyst with read-only access to all entities",
            Set.of(userRole)
        );

        log.info("Created {} role profiles", roleProfileRepository.count());

        // Create Users
        User admin = createUser("admin", "admin123", "admin@example.com", "Admin", "User");
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        userProfileService.createDefaultProfile(admin);

        User manager = createUser("manager", "manager123", "manager@example.com", "Manager", "User");
        manager.getRoles().add(managerRole);
        manager.getRoleProfiles().add(salesManagerProfile); // Example: assign profile to user
        userRepository.save(manager);
        userProfileService.createDefaultProfile(manager);

        User csrep = createUser("csrep", "csrep123", "csrep@example.com", "Customer Service", "Rep");
        csrep.getRoles().add(customerServiceRole);
        csrep.getRoleProfiles().add(customerSupportProfile); // Example: assign profile to user
        userRepository.save(csrep);
        userProfileService.createDefaultProfile(csrep);

        User basicUser = createUser("user", "user123", "user@example.com", "Basic", "User");
        basicUser.getRoles().add(userRole);
        userRepository.save(basicUser);
        userProfileService.createDefaultProfile(basicUser);

        log.info("Created {} users with profiles", userRepository.count());

        // Create Sample Customers with Accounts
        Customer johnDoe = createCustomer("John Doe", "john.doe@example.com", "555-0101", "123 Main St, Springfield", "admin");
        createAccountsForCustomer(johnDoe, "ACC-001", "ACC-002");

        Customer janeSmith = createCustomer("Jane Smith", "jane.smith@example.com", "555-0102", "456 Oak Ave, Springfield", "admin");
        createAccountsForCustomer(janeSmith, "ACC-003", "ACC-004");

        Customer bobJohnson = createCustomer("Bob Johnson", "bob.johnson@example.com", "555-0103", "789 Pine Rd, Springfield", "admin");
        createAccountsForCustomer(bobJohnson, "ACC-005", "ACC-006");

        Customer aliceWilliams = createCustomer("Alice Williams", "alice.williams@example.com", "555-0104", "321 Elm St, Springfield", "admin");
        createAccountsForCustomer(aliceWilliams, "ACC-007", "ACC-008");

        Customer charlieBrown = createCustomer("Charlie Brown", "charlie.brown@example.com", "555-0105", "654 Maple Dr, Springfield", "admin");
        createAccountsForCustomer(charlieBrown, "ACC-009", "ACC-010");

        log.info("Created {} customers", customerRepository.count());
        log.info("Created {} accounts", accountRepository.count());

        log.info("=== Data Initialization Complete ===");
        log.info("Login Credentials:");
        log.info("  Admin:    username=admin, password=admin123");
        log.info("  Manager:  username=manager, password=manager123 (has SALES_MANAGER profile)");
        log.info("  CS Rep:   username=csrep, password=csrep123 (has CUSTOMER_SUPPORT profile)");
        log.info("  User:     username=user, password=user123");
        log.info("");
        log.info("Role Inheritance:");
        log.info("  ADMIN -> MANAGER -> CUSTOMER_SERVICE -> USER");
        log.info("  (Each role inherits all permissions from its parent)");
        log.info("");
        log.info("Role Profiles:");
        log.info("  SALES_MANAGER: Combines MANAGER + CUSTOMER_SERVICE roles");
        log.info("  CUSTOMER_SUPPORT: Combines CUSTOMER_SERVICE + USER roles");
        log.info("  DATA_ANALYST: Read-only access profile");
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
            .roleProfiles(new HashSet<>())
            .build();
        return user;
    }

    private Customer createCustomer(String name, String email, String phone, String address, String createdBy) {
        if (customerRepository.findByEmail(email).isPresent()) {
            return customerRepository.findByEmail(email).get();
        }
        Customer customer = Customer.builder()
            .name(name)
            .email(email)
            .phone(phone)
            .address(address)
            .active(true)
            .build();
        // createdBy and updatedBy are set automatically by JPA auditing
        return customerRepository.save(customer);
    }

    private void createAccountsForCustomer(Customer customer, String accountRef1, String accountRef2) {
        if (customer == null) {
            return;
        }

        // Create first account (Checking Account in USD)
        if (accountRepository.findByAccountRef(accountRef1).isEmpty()) {
            Account account1 = Account.builder()
                .accountRef(accountRef1)
                .accountName(customer.getName() + " - Checking Account")
                .currency("USD")
                .status(Account.AccountStatus.ACTIVE)
                .customer(customer)
                .build();
            accountRepository.save(account1);
        }

        // Create second account (Savings Account in EUR)
        if (accountRepository.findByAccountRef(accountRef2).isEmpty()) {
            Account account2 = Account.builder()
                .accountRef(accountRef2)
                .accountName(customer.getName() + " - Savings Account")
                .currency("EUR")
                .status(Account.AccountStatus.ACTIVE)
                .customer(customer)
                .build();
            accountRepository.save(account2);
        }
    }

    private RoleProfile createRoleProfile(String name, String description, Set<Role> roles) {
        if (roleProfileRepository.findByName(name).isPresent()) {
            return roleProfileRepository.findByName(name).get();
        }
        RoleProfile profile = RoleProfile.builder()
            .name(name)
            .description(description)
            .active(true)
            .roles(roles)
            .build();
        return roleProfileRepository.save(profile);
    }
}
