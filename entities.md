# Domain Model Documentation

## Overview
This document describes the complete domain model for the anApp enterprise application implementing RBAC (Role-Based Access Control) with customer and account management.

---

## Core Entities

### Entity: User
  Description: Represents authenticated users with support for multiple roles and role profiles
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - username: String(50) [NOT NULL]
    - password: String [NOT NULL]
    - email: String(100) [NOT NULL]
    - first_name: String(50)
    - last_name: String(50)
    - enabled: Boolean [NOT NULL, DEFAULT true]
    - account_non_expired: Boolean [NOT NULL, DEFAULT true]
    - account_non_locked: Boolean [NOT NULL, DEFAULT true]
    - credentials_non_expired: Boolean [NOT NULL, DEFAULT true]
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
    - last_login: LocalDateTime
  Constraints:
    - PK(id)
    - UNIQUE(username)
    - UNIQUE(email)

---

### Entity: Role
  Description: Represents a role in the RBAC system with support for hierarchical inheritance
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - name: String(50) [NOT NULL]
    - description: String(500)
    - active: Boolean [NOT NULL, DEFAULT true]
    - parent_role_id: Long
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
  Constraints:
    - PK(id)
    - UNIQUE(name)
    - FK(parent_role_id) -> Role(id)

---

### Entity: Permission
  Description: Represents a fine-grained permission in the system
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - name: String(100) [NOT NULL]
    - description: String(500)
    - active: Boolean [NOT NULL, DEFAULT true]
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
  Constraints:
    - PK(id)
    - UNIQUE(name)

---

### Entity: RoleProfile
  Description: Allows composing multiple roles into a single profile for flexible role assignment
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - name: String(100) [NOT NULL]
    - description: String(500)
    - active: Boolean [NOT NULL, DEFAULT true]
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
    - created_by: String(50) [IMMUTABLE]
    - updated_by: String(50)
  Constraints:
    - PK(id)
    - UNIQUE(name)

---

### Entity: UserProfile
  Description: User-specific preferences and settings
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - user_id: Long [NOT NULL]
    - page_size: Integer [DEFAULT 11]
    - timezone: String(50) [DEFAULT 'UTC']
    - language: String(10) [DEFAULT 'en']
    - date_format: String(20) [DEFAULT 'yyyy-MM-dd']
    - time_format: String(20) [DEFAULT 'HH:mm:ss']
    - theme: String(20) [DEFAULT 'light']
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
    - created_by: String [IMMUTABLE]
    - updated_by: String
  Constraints:
    - PK(id)
    - FK(user_id) -> User(id)
    - UNIQUE(user_id)

---

### Entity: Customer
  Description: Represents a customer in the system
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - name: String(100) [NOT NULL]
    - email: String(100) [NOT NULL]
    - phone: String(20)
    - address: String(500)
    - active: Boolean [NOT NULL, DEFAULT true]
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
    - created_by: String [IMMUTABLE]
    - updated_by: String
  Constraints:
    - PK(id)
    - UNIQUE(email)

---

### Entity: Account
  Description: Represents a financial account belonging to a customer
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - account_ref: String(50) [NOT NULL]
    - currency: String(3) [NOT NULL]
    - account_name: String(100) [NOT NULL]
    - status: ENUM(ACTIVE, INACTIVE, CLOSED, SUSPENDED) [NOT NULL, DEFAULT ACTIVE]
    - customer_id: Long [NOT NULL]
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
    - created_by: String [IMMUTABLE]
    - updated_by: String
  Constraints:
    - PK(id)
    - FK(customer_id) -> Customer(id)
    - UNIQUE(account_ref)
    - INDEX(account_ref)
    - INDEX(customer_id)

---

### Entity: AuditLog
  Description: Stores audit trail of all significant actions in the system
  Fields:
    - id: Long [NOT NULL, AUTO_INCREMENT]
    - username: String(50) [NOT NULL]
    - action: String(50) [NOT NULL]
    - entity_type: String(100)
    - entity_id: Long
    - details: String(2000)
    - old_value: TEXT
    - new_value: TEXT
    - ip_address: String(45)
    - timestamp: LocalDateTime [NOT NULL]
  Constraints:
    - PK(id)
    - INDEX(entity_type)
    - INDEX(username)
    - INDEX(timestamp)

---

## Join Tables

### Entity: user_roles
  Description: Many-to-Many relationship between User and Role
  Fields:
    - user_id: Long [NOT NULL]
    - role_id: Long [NOT NULL]
  Constraints:
    - PK(user_id, role_id)
    - FK(user_id) -> User(id)
    - FK(role_id) -> Role(id)

---

### Entity: user_role_profiles
  Description: Many-to-Many relationship between User and RoleProfile
  Fields:
    - user_id: Long [NOT NULL]
    - role_profile_id: Long [NOT NULL]
  Constraints:
    - PK(user_id, role_profile_id)
    - FK(user_id) -> User(id)
    - FK(role_profile_id) -> RoleProfile(id)

---

### Entity: role_permissions
  Description: Many-to-Many relationship between Role and Permission
  Fields:
    - role_id: Long [NOT NULL]
    - permission_id: Long [NOT NULL]
  Constraints:
    - PK(role_id, permission_id)
    - FK(role_id) -> Role(id)
    - FK(permission_id) -> Permission(id)

---

### Entity: profile_roles
  Description: Many-to-Many relationship between RoleProfile and Role
  Fields:
    - profile_id: Long [NOT NULL]
    - role_id: Long [NOT NULL]
  Constraints:
    - PK(profile_id, role_id)
    - FK(profile_id) -> RoleProfile(id)
    - FK(role_id) -> Role(id)

---

## Base Classes

### MappedSuperclass: Auditable
  Description: Abstract base class providing automatic auditing fields
  Fields:
    - created_at: LocalDateTime [NOT NULL, IMMUTABLE]
    - updated_at: LocalDateTime
    - created_by: String [IMMUTABLE]
    - updated_by: String

  Extended by: UserProfile, Customer, Account

---

## Business Rules

### Role Inheritance
- Roles support hierarchical inheritance through parent_role_id
- Example: ADMIN -> MANAGER -> CUSTOMER_SERVICE -> USER
- getAllPermissions() recursively collects permissions from parent roles

### Permission Resolution Order
1. Direct roles assigned to user
2. Inherited permissions from parent roles
3. Roles from assigned role profiles
4. Inherited permissions from profile roles' parent roles

### Standard Permissions
- CUSTOMER_READ, CUSTOMER_WRITE, CUSTOMER_DELETE
- ACCOUNT_READ, ACCOUNT_WRITE, ACCOUNT_DELETE
- USER_MANAGE, ROLE_MANAGE, AUDIT_VIEW

### Standard Actions (AuditLog)
- CREATE, UPDATE, DELETE, LOGIN, LOGOUT, LOGIN_FAILED

### Cascade Behaviors
- User -> UserProfile: ALL operations cascade, orphan removal enabled
- Customer -> Account: ALL operations cascade, orphan removal enabled
- Role -> Role (parent-child): ALL operations cascade
- Many-to-Many relationships: PERSIST and MERGE only

### Fetch Strategies
- EAGER: User.roles, User.roleProfiles, Role.permissions, Role.parentRole, RoleProfile.roles
- LAZY: Account.customer

---

## Sample Data

### Users
- admin (password: admin123) - Full system access
- manager (password: manager123) - MANAGER role + SALES_MANAGER profile
- csrep (password: csrep123) - CUSTOMER_SERVICE role + CUSTOMER_SUPPORT profile
- user (password: user123) - USER role

### Customers
1. John Doe (john.doe@example.com)
   - ACC-001: USD Checking (ACTIVE)
   - ACC-002: EUR Savings (ACTIVE)

2. Jane Smith (jane.smith@example.com)
   - ACC-003: USD Checking (ACTIVE)
   - ACC-004: EUR Savings (ACTIVE)

3. Bob Johnson (bob.johnson@example.com)
   - ACC-005: USD Checking (ACTIVE)
   - ACC-006: EUR Savings (ACTIVE)

4. Alice Williams (alice.williams@example.com)
   - ACC-007: USD Checking (ACTIVE)
   - ACC-008: EUR Savings (ACTIVE)

5. Charlie Brown (charlie.brown@example.com)
   - ACC-009: USD Checking (ACTIVE)
   - ACC-010: EUR Savings (ACTIVE)

### Role Hierarchy
```
USER (base)
  └── CUSTOMER_SERVICE
        └── MANAGER
              └── ADMIN
```

### Role Profiles
- SALES_MANAGER: [MANAGER, CUSTOMER_SERVICE]
- CUSTOMER_SUPPORT: [CUSTOMER_SERVICE, USER]
- DATA_ANALYST: [USER]

---

## Technology Stack
- Spring Boot 3.2.1
- Java 21 LTS
- JPA/Hibernate 6
- Spring Data JPA
- Lombok
- Jackson
- BCrypt (password hashing)
- H2 (development) / PostgreSQL (production)

---

## Version
- Application Version: 1.2+
- Last Updated: 2025-11-10
