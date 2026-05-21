package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.service.AdminAuthService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuthService adminAuthService;

    public AuthController(JdbcTemplate jdbcTemplate, AdminAuthService adminAuthService) {
        this.jdbcTemplate = jdbcTemplate;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {
        String account = requestBody.get("username");
        String password = requestBody.get("password");
        String role = requestBody.get("role");

        if (account == null || password == null || role == null) {
            return Result.fail("Account, password, and role are required");
        }

        String sql;
        if ("student".equals(role)) {
            sql = "SELECT id, username, role_code, student_no, password FROM t_user WHERE student_no = ? AND role_code = 'student'";
        } else {
            sql = "SELECT id, username, role_code, student_no, password FROM t_user WHERE username = ? AND role_code = 'admin'";
        }

        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, account);
            if (users.isEmpty()) {
                return Result.fail("Invalid username or password");
            }

            Map<String, Object> userData = users.get(0);
            String dbPwd = userData.get("password") == null ? "" : String.valueOf(userData.get("password"));
            if (!passwordMatch(dbPwd, password)) {
                return Result.fail("Invalid username or password");
            }
            userData.remove("password");

            String subject = "student".equals(role)
                    ? String.valueOf(userData.getOrDefault("student_no", account))
                    : String.valueOf(userData.getOrDefault("username", account));
            String token = adminAuthService.getJwtUtil().createToken(subject, String.valueOf(userData.get("role_code")), 24 * 60 * 60);
            userData.put("token", token);

            return Result.success("Login succeeded", userData);
        } catch (Exception e) {
            return Result.fail("Database query failed: " + e.getMessage());
        }
    }

    private boolean passwordMatch(String dbPassword, String inputPassword) {
        String db = normalize(dbPassword);
        String in = normalize(inputPassword);
        if (db.isEmpty() || in.isEmpty()) return false;
        if (db.equals(in)) return true;

        boolean dbMd5 = isMd5(db);
        boolean inMd5 = isMd5(in);

        if (dbMd5 && !inMd5) {
            return db.equalsIgnoreCase(md5Hex(in));
        }
        if (!dbMd5 && inMd5) {
            return in.equalsIgnoreCase(md5Hex(db));
        }
        return false;
    }

    private boolean isMd5(String v) {
        if (v == null) return false;
        String s = v.trim();
        if (s.length() != 32) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ok = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f')
                    || (c >= 'A' && c <= 'F');
            if (!ok) return false;
        }
        return true;
    }

    private String md5Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            return "";
        }
    }

    private String normalize(String v) {
        return v == null ? "" : v.trim();
    }
}
