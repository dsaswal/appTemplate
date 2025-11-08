package dev.dsa.service;

import dev.dsa.entity.AuditLog;
import dev.dsa.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String action, String entityType, Long entityId, String details, String oldValue, String newValue) {
        String username = getCurrentUsername();
        String ipAddress = getClientIpAddress();

        AuditLog auditLog = AuditLog.builder()
            .username(username)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .oldValue(oldValue)
            .newValue(newValue)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .build();

        auditLogRepository.save(auditLog);
        log.info("Audit logged - User: {}, Action: {}, Entity: {}, ID: {}", username, action, entityType, entityId);
    }

    @Transactional
    public void logLoginSuccess(String username, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
            .username(username)
            .action("LOGIN_SUCCESS")
            .details("User logged in successfully")
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .build();

        auditLogRepository.save(auditLog);
        log.info("Login success logged for user: {}", username);
    }

    @Transactional
    public void logLoginFailure(String username, String ipAddress, String reason) {
        AuditLog auditLog = AuditLog.builder()
            .username(username)
            .action("LOGIN_FAILURE")
            .details("Login failed: " + reason)
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .build();

        auditLogRepository.save(auditLog);
        log.info("Login failure logged for user: {}", username);
    }

    @Transactional
    public void logLogout(String username, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
            .username(username)
            .action("LOGOUT")
            .details("User logged out")
            .ipAddress(ipAddress)
            .timestamp(LocalDateTime.now())
            .build();

        auditLogRepository.save(auditLog);
        log.info("Logout logged for user: {}", username);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findTop100ByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (Exception e) {
            log.debug("Could not get IP address: {}", e.getMessage());
            return "unknown";
        }
    }
}
