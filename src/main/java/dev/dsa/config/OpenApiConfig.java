package dev.dsa.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "anApp REST API",
        version = "1.2",
        description = """
            RESTful API for anApp - Enterprise Application with Role-Based Access Control (RBAC).

            ## Features
            - JWT-based authentication and authorization
            - Customer and Account management
            - User and Role management
            - Permission-based access control
            - Role Profiles for flexible role composition

            ## Authentication
            1. Call POST /api/auth/login with username and password to get a JWT token
            2. Use the token in the Authorization header: `Bearer <token>`
            3. Token is valid for 24 hours

            ## Authorization
            - Different endpoints require different permissions or roles
            - Admin endpoints require ROLE_ADMIN
            - Business endpoints require specific permissions (e.g., CUSTOMER_READ, ACCOUNT_WRITE)
            """,
        contact = @Contact(
            name = "API Support",
            email = "support@anapp.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            description = "Local Development Server",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Production Server",
            url = "https://api.anapp.com"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "JWT authentication token. Obtain by calling POST /api/auth/login with username and password."
)
public class OpenApiConfig {
}
