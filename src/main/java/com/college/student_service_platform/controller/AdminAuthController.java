package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.AdminLoginRequest;
import com.college.student_service_platform.dto.AdminLoginResponse;
import com.college.student_service_platform.service.AdminAuthService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/geeker")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final JdbcTemplate jdbcTemplate;

    public AdminAuthController(AdminAuthService adminAuthService, JdbcTemplate jdbcTemplate) {
        this.adminAuthService = adminAuthService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        String token = adminAuthService.loginAdmin(request == null ? null : request.getUsername(), request == null ? null : request.getPassword());
        return Result.success("登录成功", new AdminLoginResponse(token));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(
            @RequestHeader(value = "x-access-token", required = false) String xAccessToken,
            @RequestHeader(value = "authorization", required = false) String authorization
    ) {
        String token = xAccessToken;
        if (token == null || token.isBlank()) {
            token = authorization;
            if (token != null && token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7);
            }
        }
        Map<String, Object> payload = adminAuthService.getJwtUtil().verifyToken(token);
        String username = String.valueOf(payload.getOrDefault("sub", ""));

        String sql = """
                SELECT id, username, role_code, student_no, wechat_openid, status, created_at, updated_at
                FROM t_user
                WHERE username = ? AND role_code = 'admin'
                """;

        Map<String, Object> data = jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("管理员不存在");
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getLong("id"));
            m.put("username", rs.getString("username"));
            m.put("role_code", rs.getString("role_code"));
            m.put("student_no", rs.getString("student_no"));
            m.put("wechat_openid", rs.getString("wechat_openid"));
            m.put("status", rs.getInt("status"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            m.put("created_at", createdAt == null ? null : createdAt.toLocalDateTime());
            m.put("updated_at", updatedAt == null ? null : updatedAt.toLocalDateTime());
            return m;
        }, username);

        return Result.success(data);
    }
}
