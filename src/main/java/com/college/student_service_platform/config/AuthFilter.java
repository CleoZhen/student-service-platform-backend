package com.college.student_service_platform.config;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.service.AdminAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final AdminAuthService adminAuthService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthFilter(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return true;
        if (!uri.startsWith("/api/")) return true;
        if (uri.equals("/api/health")) return true;
        if (uri.startsWith("/api/test/")) return true;
        if (uri.equals("/api/geeker/login")) return true;
        if (uri.startsWith("/api/file/download/")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("x-access-token");
        if (token == null || token.isBlank()) {
            token = request.getHeader("authorization");
            if (token != null && token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7);
            }
        }

        if (token == null || token.isBlank()) {
            writeJson(response, Result.fail(401, "未登录"));
            return;
        }

        try {
            Map<String, Object> payload = adminAuthService.getJwtUtil().verifyToken(token);
            Object role = payload.get("role");
            if (role == null || !"admin".equals(String.valueOf(role))) {
                writeJson(response, Result.fail(403, "无权限"));
                return;
            }
        } catch (Exception e) {
            writeJson(response, Result.fail(401, "登录失效"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, Result<Void> body) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(mapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8));
    }
}

