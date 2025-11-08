# Enterprise Spring Boot Application

A production-ready, enterprise-grade Spring Boot application with comprehensive RBAC (Role-Based Access Control), dynamic permissions, audit logging, and admin UI.

## Features

- **Role-Based Access Control (RBAC)**: Complete user, role, and permission management
- **Dynamic Permissions**: Change roles and permissions without application restart
- **Role Inheritance**: Roles can inherit from parent roles (e.g., ADMIN -> MANAGER -> CUSTOMER_SERVICE -> USER)
- **Audit Logging**: Complete audit trail for all business and user management operations
- **Admin UI**: Full-featured administration interface using Thymeleaf
- **H2 Database**: In-memory database for development (easily switchable to PostgreSQL)
- **Customer Management**: Sample business entity with full CRUD operations
- **Enterprise Security**: Spring Security with BCrypt password encoding

## Technology Stack

- Java 21
- Spring Boot 3.5.8
- Spring Security 6
- Spring Data JPA
- Thymeleaf + Bootstrap 5
- H2 Database (development) / PostgreSQL (production)
- Lombok
- Maven

## Prerequisites (Windows)

1. **Java Development Kit (JDK) 21**
   - Download from: https://adoptium.net/temurin/releases/
   - Choose: Windows x64, JDK 21 (LTS)
   - Install and set JAVA_HOME environment variable
   - Verify: Open Command Prompt and run `java -version`

2. **Apache Maven**
   - Download from: https://maven.apache.org/download.cgi
   - Extract to `C:\Program Files\Apache\maven`
   - Add `C:\Program Files\Apache\maven\bin` to PATH environment variable
   - Verify: Open Command Prompt and run `mvn -version`

3. **Git for Windows**
   - Download from: https://git-scm.com/download/win
   - Install with default options
   - Verify: Open Command Prompt and run `git --version`

## Cloning the Repository on Windows

1. **Open Command Prompt or PowerShell**
   ```cmd
   # Navigate to your desired directory
   cd C:\Users\YourUsername\Documents

   # Clone the repository
   git clone https://github.com/dsaswal/appTemplate.git

   # Navigate to the project directory
   cd appTemplate
   ```

2. **Alternative: Using Git Bash**
   - Right-click in your desired folder
   - Select "Git Bash Here"
   ```bash
   git clone https://github.com/dsaswal/appTemplate.git
   cd appTemplate
   ```

## Running the Application on Windows

### Method 1: Using Maven (Recommended)

1. **Open Command Prompt or PowerShell in the project directory**
   ```cmd
   cd C:\Users\YourUsername\Documents\appTemplate
   ```

2. **Run the application**
   ```cmd
   mvnw.cmd spring-boot:run
   ```

3. **Access the application**
   - Open your browser and navigate to: http://localhost:8080
   - You will be redirected to the login page

### Method 2: Using IDE (IntelliJ IDEA / Eclipse)

#### IntelliJ IDEA
1. Open IntelliJ IDEA
2. File -> Open -> Select the `appTemplate` folder
3. Wait for Maven dependencies to download
4. Right-click on `AppTemplateApplication.java`
5. Select "Run 'AppTemplateApplication'"

#### Eclipse
1. Open Eclipse
2. File -> Import -> Existing Maven Projects
3. Browse to the `appTemplate` folder
4. Right-click on the project -> Run As -> Spring Boot App

### Method 3: Building and Running JAR

1. **Build the JAR file**
   ```cmd
   mvnw.cmd clean package
   ```

2. **Run the JAR**
   ```cmd
   java -jar target\dsa-0.0.1-SNAPSHOT.jar
   ```

## Default Login Credentials

The application comes with pre-configured users demonstrating role inheritance:

| Username | Password    | Role              | Permissions                           |
|----------|-------------|-------------------|---------------------------------------|
| admin    | admin123    | ADMIN             | All permissions (inherits all below)  |
| manager  | manager123  | MANAGER           | Customer + Delete + Audit View        |
| csrep    | csrep123    | CUSTOMER_SERVICE  | Customer Read + Write                 |
| user     | user123     | USER              | Basic access                          |

## Application Structure

```
appTemplate/
├── src/main/java/dev/dsa/
│   ├── config/             # Configuration classes
│   │   ├── CacheConfig.java
│   │   ├── DataInitializer.java
│   │   └── SecurityConfig.java
│   ├── controller/         # Web controllers
│   │   ├── AdminController.java
│   │   ├── CustomerController.java
│   │   └── HomeController.java
│   ├── entity/             # JPA entities
│   │   ├── AuditLog.java
│   │   ├── Customer.java
│   │   ├── Permission.java
│   │   ├── Role.java
│   │   └── User.java
│   ├── repository/         # Data repositories
│   ├── service/            # Business logic
│   │   ├── AuditService.java
│   │   ├── CustomerService.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── RbacService.java
│   │   └── UserService.java
│   └── AppTemplateApplication.java
├── src/main/resources/
│   ├── templates/          # Thymeleaf templates
│   │   ├── admin/          # Admin UI
│   │   └── customers/      # Customer UI
│   ├── static/             # CSS, JS, images
│   └── application.properties
└── pom.xml
```

## Key Features Guide

### 1. User Management
- Navigate to: Administration -> User Management
- Create, edit, and delete users
- Assign multiple roles to users
- Enable/disable user accounts

### 2. Role Management
- Navigate to: Administration -> Role Management
- Create roles with parent-child relationships
- Assign permissions to roles
- View effective permissions (including inherited)

### 3. Permission Management
- Navigate to: Administration -> Permissions
- Create new permissions
- Enable/disable permissions dynamically (no restart required)

### 4. Customer Management
- Navigate to: Customers
- Full CRUD operations on customer entities
- Permission-based access control
- Automatic audit logging

### 5. Audit Logs
- Navigate to: Administration -> Audit Logs
- View all user activities
- Track login/logout events
- Monitor data changes with before/after values

## H2 Database Console

Access the H2 console for database inspection:

1. URL: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:enterprisedb`
3. Username: `sa`
4. Password: (leave blank)

## Switching to PostgreSQL

To switch from H2 to PostgreSQL for production:

1. **Install PostgreSQL** on Windows
   - Download from: https://www.postgresql.org/download/windows/
   - Install and remember your postgres user password

2. **Create Database**
   ```sql
   CREATE DATABASE enterprisedb;
   ```

3. **Update `application.properties`**
   ```properties
   # Comment out H2 configuration
   #spring.datasource.url=jdbc:h2:mem:enterprisedb

   # Uncomment PostgreSQL configuration
   spring.datasource.url=jdbc:postgresql://localhost:5432/enterprisedb
   spring.datasource.username=postgres
   spring.datasource.password=yourpassword
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   ```

## Common Issues on Windows

### Maven Command Not Found
- Ensure Maven bin directory is in PATH
- Restart Command Prompt after setting PATH

### Port 8080 Already in Use
- Change port in `application.properties`:
  ```properties
  server.port=8081
  ```

### Java Version Issues
- Verify Java 21 is installed: `java -version`
- Set JAVA_HOME to JDK 21 installation directory

## Development Tips

### Hot Reload (DevTools)
Add Spring Boot DevTools for automatic restarts during development:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Logging Levels
Adjust logging in `application.properties`:
```properties
logging.level.dev.dsa=DEBUG
logging.level.org.springframework.security=INFO
```

## Security Notes

- Default passwords are for **development only**
- Change all passwords before deploying to production
- Use environment variables for sensitive configuration
- Enable HTTPS in production
- Implement password complexity rules
- Add account lockout after failed login attempts

## Testing

Run tests with:
```cmd
mvnw.cmd test
```

## Building for Production

```cmd
mvnw.cmd clean package -DskipTests
```

The JAR file will be created in the `target` directory.

## Support

For issues and questions:
- Check the application logs in the console
- Review H2 console for data issues
- Check Spring Boot documentation: https://spring.io/projects/spring-boot

## License

This is a template project for enterprise applications.

## Author

DSA - Enterprise Application Template
