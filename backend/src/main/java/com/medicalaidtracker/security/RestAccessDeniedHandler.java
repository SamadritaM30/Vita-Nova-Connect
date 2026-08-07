package com.medicalaidtracker.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        var body = new LinkedHashMap<String, Object>();
        body.put("type", "https://medical-aid-tracker.dev/problems/access-denied");
        body.put("title", "Forbidden");
        body.put("status", 403);
        body.put("detail", "You do not have permission to perform this action.");
        body.put("instance", request.getRequestURI());
        body.put("code", "ACCESS_DENIED");
        body.put("timestamp", Instant.now());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
