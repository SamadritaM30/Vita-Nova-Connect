package com.medicalaidtracker.audit;

import com.medicalaidtracker.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(User actor, String action, String resourceType, Object resourceId, String details) {
        var entry = new AuditLog();
        entry.setActor(actor);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(String.valueOf(resourceId));
        entry.setDetails(details);
        entry.setIpAddress(currentIpAddress());
        auditLogRepository.save(entry);
        log.info("audit action={} resourceType={} resourceId={} actor={}",
                action, resourceType, resourceId, actor == null ? "system" : actor.getId());
    }

    private String currentIpAddress() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRemoteAddr();
        }
        return null;
    }
}
