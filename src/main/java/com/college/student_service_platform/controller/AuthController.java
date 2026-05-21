package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    // 自动注入本地数据库
    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 专门为微信小程序提供 POST 方式的登录接口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> requestBody) {

        // 从 JSON 中提取账号、密码和角色
        String account = requestBody.get("username");
        String password = requestBody.get("password");
        String role = requestBody.get("role");

        // 如果账号、密码或角色为空，直接返回失败
        if (account == null || password == null || role == null) {
            return Result.fail("账号、密码或角色不能为空");
        }

        String sql;
        // 根据前端传来的角色，动态决定去查数据库的哪个字段
        if ("student".equals(role)) {
            // 学生角色：拿着前端传来的学号，去匹配数据库的 student_no 字段
            sql = "SELECT id, username, role_code, student_no, password FROM t_user WHERE student_no = ? AND role_code = 'student'";
        } else {
            // 管理员角色：拿着前端传来的账号，去匹配数据库的 username 字段
            sql = "SELECT id, username, role_code, student_no, password FROM t_user WHERE username = ? AND role_code = 'admin'";
        }

        try {
            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, account);
            if (!users.isEmpty()) {
                Map<String, Object> userData = users.get(0);
                String dbPwd = userData.get("password") == null ? "" : String.valueOf(userData.get("password"));
                if (!passwordMatch(dbPwd, password)) {
                    return Result.fail("用户名或密码错误");
                }
                userData.remove("password");

                // 【核心修复】：生成安全的 Token。
                // 为了防止 account 里带有中文导致前端 Header 报错（ISO-8859-1 错误），
                // 我们对 account 进行 URL 编码，确保 Token 字符串里只有安全的 ASCII 字符。
                String safeAccount = URLEncoder.encode(account, StandardCharsets.UTF_8.toString());
                userData.put("token", "mock_jwt_token_for_" + safeAccount);

                return Result.success("登录成功", userData);
            } else {
                return Result.fail("用户名或密码错误");
            }
        } catch (Exception e) {
            return Result.fail("数据库查询异常: " + e.getMessage());
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
