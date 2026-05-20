package com.college.student_service_platform.service;

import com.college.student_service_platform.common.JwtUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class AdminAuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;

    public AdminAuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = "student-service-platform-dev-secret";
        }
        this.jwtUtil = new JwtUtil(secret);
    }

    public String loginAdmin(String username, String password) {
        String u = normalize(username);
        String p = normalize(password);
        if (u.isEmpty() || p.isEmpty()) {
            throw new IllegalArgumentException("用户名或密码不能为空");
        }

        String sql = """
                SELECT password, status
                FROM t_user
                WHERE username = ? AND role_code = 'admin'
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                throw new IllegalArgumentException("账号或密码错误");
            }
            String dbPwd = rs.getString("password");
            int status = rs.getInt("status");
            if (status == 0) {
                throw new IllegalArgumentException("账号已禁用");
            }
            if (!passwordMatch(dbPwd, p)) {
                throw new IllegalArgumentException("账号或密码错误");
            }
            return jwtUtil.createToken(u, "admin", Duration.ofHours(24).toSeconds());
        }, u);
    }

    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    private boolean passwordMatch(String dbPassword, String inputPassword) {
        String db = normalize(dbPassword);
        if (db.isEmpty()) return false;
        if (db.equals(inputPassword)) return true;
        return md5Hex(db).equalsIgnoreCase(inputPassword);
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

