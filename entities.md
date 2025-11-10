# Domain Model Documentation

## Overview

This document describes the complete domain model for the anApp enterprise application. The application implements a comprehensive RBAC (Role-Based Access Control) system with customer and account management capabilities.

## Entity Relationship Diagram

```
┌─────────────────┐
│     User        │
├─────────────────┤
│ id (PK)         │
│ username        │
│ password        │
│ email           │
│ firstName       │
│ lastName        │
│ enabled         │
│ accountNonExp.. │
│ accountNonLoc.. │
│ credentialsNo.. │
│ createdAt       │
│ updatedAt       │
│ lastLogin       │
└────────┬────────┘
         │ 1:1
         ├──────────────────┐
         │                  │
         ▼                  ▼
┌─────────────────┐  ┌─────────────────┐
│  UserProfile    │  │  RoleProfile    │
├─────────────────┤  ├─────────────────┤
│ id (PK)         │  │ id (PK)         │
│ user_id (FK)    │  │ name            │
│ pageSize        │  │ description     │
│ timezone        │  │ active          │
│ language        │  │ createdAt       │
│ dateFormat      │  │ updatedAt       │
│ timeFormat      │  │ createdBy       │
│ theme           │  │ updatedBy       │
│ (+ Auditable)   │  └────────┬────────┘
└─────────────────┘           │ M:N
                              │
         ┌────────────────────┘
         │
         ▼
┌─────────────────┐
│      Role       │
├─────────────────┤
│ id (PK)         │
│ name            │
│ description     │
│ active          │
│ parent_role_id  │◄───┐ Self-referential
│ createdAt       │    │ (Role Inheritance)
│ updatedAt       │    │
└────────┬────────┘    │
         │ M:N         │
         │             │
         ▼             │
┌─────────────────┐    │
│   Permission    │    │
├─────────────────┤    │
│ id (PK)         │    │
│ name            │    │
│ description     │    │
│ active          │    │
│ createdAt       │    │
│ updatedAt       │    │
└─────────────────┘    │
                       │
┌─────────────────┐    │
│    Customer     │    │
├─────────────────┤    │
│ id (PK)         │    │
│ name            │    │
│ email           │    │
│ phone           │    │
│ address         │    │
│ active          │    │
│ (+ Auditable)   │    │
└────────┬────────┘    │
         │ 1:N         │
         │             │
         ▼             │
┌─────────────────┐    │
│    Account      │    │
├─────────────────┤    │
│ id (PK)         │    │
│ accountRef      │    │
│ currency        │    │
│ accountName     │    │
│ status          │    │
│ customer_id (FK)│    │
│ (+ Auditable)   │    │
└─────────────────┘    │
                       │
┌─────────────────┐    │
│   AuditLog      │    │
├─────────────────┤    │
│ id (PK)         │    │
│ username        │    │
│ action          │    │
│ entityType      │    │
│ entityId        │    │
│ details         │    │
│ oldValue        │    │
│ newValue        │    │
│ ipAddress       │    │
│ timestamp       │    │
└─────────────────┘    │
```

## Core Entities

### 1. User

**Table:** `users`

**Description:** Represents authenticated users of the application with support for multiple roles and role profiles.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| username | String(50) | NOT NULL, UNIQUE | Login username |
| password | String | NOT NULL | BCrypt hashed password |
| email | String(100) | NOT NULL, UNIQUE | User email address |
| firstName | String(50) | - | User's first name |
| lastName | String(50) | - | User's last name |
| enabled | Boolean | NOT NULL, Default: true | Account enabled status |
| accountNonExpired | Boolean | NOT NULL, Default: true | Account expiration status |
| accountNonLocked | Boolean | NOT NULL, Default: true | Account lock status |
| credentialsNonExpired | Boolean | NOT NULL, Default: true | Password expiration status |
| createdAt | LocalDateTime | NOT NULL | Record creation timestamp |
| updatedAt | LocalDateTime | - | Last update timestamp |
| lastLogin | LocalDateTime | - | Last successful login |

**Relationships:**
- **Many-to-Many** with `Role` (through `user_roles` join table)
- **Many-to-Many** with `RoleProfile` (through `user_role_profiles` join table)
- **One-to-One** with `UserProfile` (bidirectional)

**Business Methods:**
- `getAllPermissions()`: Returns aggregated permissions from all roles and profiles (including inherited)
- `getAllRoles()`: Returns all roles (direct + from profiles)

**Fetch Strategy:** EAGER for roles and roleProfiles

---

### 2. Role

**Table:** `roles`

**Description:** Represents a role in the RBAC system with support for hierarchical inheritance.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| name | String(50) | NOT NULL, UNIQUE | Role name (e.g., "ADMIN", "MANAGER") |
| description | String(500) | - | Role description |
| active | Boolean | NOT NULL, Default: true | Active status |
| parent_role_id | Long | Foreign Key | Parent role for inheritance |
| createdAt | LocalDateTime | NOT NULL | Record creation timestamp |
| updatedAt | LocalDateTime | - | Last update timestamp |

**Relationships:**
- **Self-referential Many-to-One** with `Role` (parent role) - Enables role inheritance
- **One-to-Many** with `Role` (child roles)
- **Many-to-Many** with `Permission` (through `role_permissions` join table)

**Business Methods:**
- `getAllPermissions()`: Returns all permissions including inherited from parent roles (recursive)

**Role Inheritance Example:**
```
ADMIN → MANAGER → CUSTOMER_SERVICE → USER
```
When a user has the ADMIN role, they inherit all permissions from ADMIN, MANAGER, CUSTOMER_SERVICE, and USER.

**Fetch Strategy:** EAGER for parentRole and permissions

---

### 3. Permission

**Table:** `permissions`

**Description:** Represents a fine-grained permission in the system.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| name | String(100) | NOT NULL, UNIQUE | Permission name (e.g., "CUSTOMER_READ") |
| description | String(500) | - | Permission description |
| active | Boolean | NOT NULL, Default: true | Active status |
| createdAt | LocalDateTime | NOT NULL | Record creation timestamp |
| updatedAt | LocalDateTime | - | Last update timestamp |

**Relationships:**
- **Many-to-Many** with `Role` (inverse side)

**Standard Permissions:**
- `CUSTOMER_READ` - Read customer data
- `CUSTOMER_WRITE` - Create and update customers
- `CUSTOMER_DELETE` - Delete customers
- `ACCOUNT_READ` - Read account data
- `ACCOUNT_WRITE` - Create and update accounts
- `ACCOUNT_DELETE` - Delete accounts
- `USER_MANAGE` - Manage users
- `ROLE_MANAGE` - Manage roles and permissions
- `AUDIT_VIEW` - View audit logs

---

### 4. RoleProfile

**Table:** `role_profiles`

**Description:** Allows composing multiple roles into a single profile for flexible role assignment without multiple inheritance complexity.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| name | String(100) | NOT NULL, UNIQUE | Profile name |
| description | String(500) | - | Profile description |
| active | Boolean | NOT NULL, Default: true | Active status |
| createdAt | LocalDateTime | NOT NULL | Record creation timestamp |
| updatedAt | LocalDateTime | - | Last update timestamp |
| createdBy | String(50) | - | Creator username |
| updatedBy | String(50) | - | Last modifier username |

**Relationships:**
- **Many-to-Many** with `Role` (through `profile_roles` join table)
- **Many-to-Many** with `User` (inverse side)

**Business Methods:**
- `getAllPermissions()`: Returns aggregated permissions from all roles in the profile
- `addRole(Role role)`: Adds a role to the profile
- `removeRole(Role role)`: Removes a role from the profile

**Example Profiles:**
- `SALES_MANAGER` = [MANAGER, CUSTOMER_SERVICE]
- `CUSTOMER_SUPPORT` = [CUSTOMER_SERVICE, USER]
- `DATA_ANALYST` = [USER] (read-only access)

**Fetch Strategy:** EAGER for roles

---

### 5. UserProfile

**Table:** `user_profiles`

**Description:** User-specific preferences and settings.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| user_id | Long | Foreign Key, NOT NULL, UNIQUE | Reference to User |
| pageSize | Integer | Default: 11 | Pagination page size |
| timezone | String(50) | Default: "UTC" | User timezone |
| language | String(10) | Default: "en" | Preferred language |
| dateFormat | String(20) | Default: "yyyy-MM-dd" | Date format preference |
| timeFormat | String(20) | Default: "HH:mm:ss" | Time format preference |
| theme | String(20) | Default: "light" | UI theme (light/dark) |

**Extends:** `Auditable` (includes createdAt, updatedAt, createdBy, updatedBy)

**Relationships:**
- **One-to-One** with `User`

---

### 6. Customer

**Table:** `customers`

**Description:** Represents a customer in the system.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| name | String(100) | NOT NULL | Customer name |
| email | String(100) | NOT NULL, UNIQUE | Customer email address |
| phone | String(20) | - | Phone number |
| address | String(500) | - | Physical address |
| active | Boolean | NOT NULL, Default: true | Active status |

**Extends:** `Auditable` (includes createdAt, updatedAt, createdBy, updatedBy)

**Relationships:**
- **One-to-Many** with `Account` (bidirectional, cascade ALL, orphanRemoval)

**Business Methods:**
- `addAccount(Account account)`: Adds an account and establishes bidirectional relationship
- `removeAccount(Account account)`: Removes an account and clears the relationship

**Jackson Annotations:**
- `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` - Prevents Hibernate proxy serialization errors

---

### 7. Account

**Table:** `accounts`

**Description:** Represents a financial account belonging to a customer.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| accountRef | String(50) | NOT NULL, UNIQUE | Account reference number |
| currency | String(3) | NOT NULL | Currency code (USD, EUR, etc.) |
| accountName | String(100) | NOT NULL | Account name/description |
| status | AccountStatus | NOT NULL, Default: ACTIVE | Account status enum |
| customer_id | Long | Foreign Key, NOT NULL | Reference to Customer |

**Extends:** `Auditable` (includes createdAt, updatedAt, createdBy, updatedBy)

**Enums:**
- `AccountStatus`: ACTIVE, INACTIVE, CLOSED, SUSPENDED

**Relationships:**
- **Many-to-One** with `Customer` (LAZY fetch)

**Indexes:**
- `idx_account_ref` on `accountRef`
- `idx_customer_id` on `customer_id`

**Jackson Annotations:**
- `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` - At class level
- `@JsonIgnoreProperties({"accounts"})` - On customer field to prevent circular reference

**Fetch Strategy:** LAZY for customer relationship

---

### 8. AuditLog

**Table:** `audit_logs`

**Description:** Stores audit trail of all significant actions in the system.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| id | Long | Primary Key, Auto-increment | Unique identifier |
| username | String(50) | NOT NULL | User who performed the action |
| action | String(50) | NOT NULL | Action type (CREATE, UPDATE, DELETE, LOGIN, LOGOUT) |
| entityType | String(100) | - | Entity type affected (User, Role, Customer, etc.) |
| entityId | Long | - | ID of affected entity |
| details | String(2000) | - | Additional details about the action |
| oldValue | TEXT | - | Previous value (for updates) |
| newValue | TEXT | - | New value (for updates/creates) |
| ipAddress | String(45) | - | Client IP address |
| timestamp | LocalDateTime | NOT NULL | When the action occurred |

**Indexes:**
- `idx_entity_type` on `entityType`
- `idx_username` on `username`
- `idx_timestamp` on `timestamp`

**Standard Actions:**
- `CREATE` - Entity creation
- `UPDATE` - Entity modification
- `DELETE` - Entity deletion
- `LOGIN` - Successful login
- `LOGOUT` - User logout
- `LOGIN_FAILED` - Failed login attempt

---

## Base Classes

### Auditable (MappedSuperclass)

**Description:** Abstract base class that provides automatic auditing fields for entities.

**Fields:**

| Field Name | Type | Constraints | Description |
|------------|------|-------------|-------------|
| createdAt | LocalDateTime | NOT NULL, Immutable | When record was created |
| updatedAt | LocalDateTime | - | When record was last updated |
| createdBy | String | Immutable | Username who created the record |
| updatedBy | String | - | Username who last updated the record |

**Annotations:**
- `@EntityListeners(AuditingEntityListener.class)` - Enables JPA auditing
- `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` - Spring Data JPA audit annotations

**Entities that extend Auditable:**
- `UserProfile`
- `Customer`
- `Account`

---

## Join Tables

### user_roles

**Description:** Many-to-Many relationship between User and Role

| Column | Type | Description |
|--------|------|-------------|
| user_id | Long | Foreign Key to users.id |
| role_id | Long | Foreign Key to roles.id |

**Composite Primary Key:** (user_id, role_id)

---

### user_role_profiles

**Description:** Many-to-Many relationship between User and RoleProfile

| Column | Type | Description |
|--------|------|-------------|
| user_id | Long | Foreign Key to users.id |
| role_profile_id | Long | Foreign Key to role_profiles.id |

**Composite Primary Key:** (user_id, role_profile_id)

---

### role_permissions

**Description:** Many-to-Many relationship between Role and Permission

| Column | Type | Description |
|--------|------|-------------|
| role_id | Long | Foreign Key to roles.id |
| permission_id | Long | Foreign Key to permissions.id |

**Composite Primary Key:** (role_id, permission_id)

---

### profile_roles

**Description:** Many-to-Many relationship between RoleProfile and Role

| Column | Type | Description |
|--------|------|-------------|
| profile_id | Long | Foreign Key to role_profiles.id |
| role_id | Long | Foreign Key to roles.id |

**Composite Primary Key:** (profile_id, role_id)

---

## Permission Resolution Flow

When checking if a user has a specific permission, the system follows this resolution order:

1. **Direct Roles:** Check permissions from roles directly assigned to the user
2. **Inherited Permissions:** For each direct role, recursively check parent roles
3. **Profile Roles:** Check permissions from roles within assigned role profiles
4. **Profile Inherited Permissions:** For each profile role, recursively check parent roles

### Example

```
User: manager
├── Direct Roles: [MANAGER]
│   └── Inherited from: CUSTOMER_SERVICE → USER
├── Role Profiles: [SALES_MANAGER]
│   └── Contains Roles: [MANAGER, CUSTOMER_SERVICE]
│       └── Each with their own inheritance chain
```

**Result:** User "manager" has all permissions from:
- MANAGER (direct)
- CUSTOMER_SERVICE (inherited from MANAGER)
- USER (inherited from CUSTOMER_SERVICE)
- Plus all permissions from SALES_MANAGER profile

---

## JSON Serialization Considerations

### Hibernate Proxy Handling

All entities that use lazy loading are configured with Jackson annotations to prevent serialization errors:

1. **Class-level annotation:**
   ```java
   @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
   ```
   Applied to: `Account`, `Customer`

2. **Field-level annotation for circular references:**
   ```java
   @JsonIgnoreProperties({"accounts"})  // On Account.customer field
   ```

3. **Jackson Hibernate6Module Configuration:**
   - Disables `FORCE_LAZY_LOADING` to avoid N+1 queries
   - Enables `SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS` to serialize only IDs for uninitialized lazy entities

---

## Database Schema Notes

### Cascade Operations

- **User → UserProfile:** ALL operations cascade, orphanRemoval enabled
- **Customer → Account:** ALL operations cascade, orphanRemoval enabled
- **Role → Role (parent-child):** ALL operations cascade
- **User ↔ Role:** PERSIST and MERGE only (no automatic deletion)
- **User ↔ RoleProfile:** PERSIST and MERGE only
- **Role ↔ Permission:** PERSIST and MERGE only
- **RoleProfile ↔ Role:** PERSIST and MERGE only

### Fetch Strategies

**EAGER Fetching:**
- User.roles
- User.roleProfiles
- Role.permissions
- Role.parentRole
- RoleProfile.roles

**LAZY Fetching:**
- Account.customer

### Unique Constraints

- User.username
- User.email
- Role.name
- Permission.name
- RoleProfile.name
- Customer.email
- Account.accountRef
- UserProfile.user_id

---

## Sample Data

The application initializes with the following sample data:

### Users
- **admin** (password: admin123) - Full system access
- **manager** (password: manager123) - Manager role + SALES_MANAGER profile
- **csrep** (password: csrep123) - Customer service role + CUSTOMER_SUPPORT profile
- **user** (password: user123) - Basic user role

### Customers (5 total)
Each customer has 2 accounts:

1. **John Doe** - ACC-001 (USD Checking), ACC-002 (EUR Savings)
2. **Jane Smith** - ACC-003 (USD Checking), ACC-004 (EUR Savings)
3. **Bob Johnson** - ACC-005 (USD Checking), ACC-006 (EUR Savings)
4. **Alice Williams** - ACC-007 (USD Checking), ACC-008 (EUR Savings)
5. **Charlie Brown** - ACC-009 (USD Checking), ACC-010 (EUR Savings)

### Role Hierarchy
```
USER (base role)
  └── CUSTOMER_SERVICE (inherits from USER)
        └── MANAGER (inherits from CUSTOMER_SERVICE)
              └── ADMIN (inherits from MANAGER)
```

### Role Profiles
- **SALES_MANAGER:** Combines MANAGER + CUSTOMER_SERVICE
- **CUSTOMER_SUPPORT:** Combines CUSTOMER_SERVICE + USER
- **DATA_ANALYST:** Read-only access (USER role)

---

## Technology Stack

- **JPA/Hibernate 6:** ORM and persistence
- **Spring Data JPA:** Repository abstraction and auditing
- **Lombok:** Reduces boilerplate code
- **Jackson:** JSON serialization/deserialization
- **BCrypt:** Password hashing
- **H2 Database:** In-memory database (development)
- **PostgreSQL:** Production database support

---

## API Access Control

All API endpoints respect the RBAC permissions:

- `/api/auth/**` - Public access (login endpoints)
- `/api/admin/**` - Requires ADMIN role
- `/api/customers/**` - Requires CUSTOMER_READ or CUSTOMER_WRITE permissions
- `/api/accounts/**` - Requires ACCOUNT_READ or ACCOUNT_WRITE permissions

Method-level security is enforced using `@PreAuthorize` annotations.

---

## Version Information

- **Spring Boot:** 3.2.1
- **Java:** 21 LTS
- **Application Version:** 1.2+
- **Last Updated:** 2025-11-10
