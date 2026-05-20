package com.college.student_service_platform.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class JwtUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final byte[] secret;

    public JwtUtil(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret 不能为空");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String createToken(String subject, String roleCode, long ttlSeconds) {
        long now = Instant.now().getEpochSecond();
        long exp = now + Math.max(60, ttlSeconds);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", subject);
        payload.put("role", roleCode);
        payload.put("iat", now);
        payload.put("exp", exp);

        String headerPart = base64Url(json(header));
        String payloadPart = base64Url(json(payload));
        String signingInput = headerPart + "." + payloadPart;
        String signature = base64Url(hmacSha256(signingInput));
        return signingInput + "." + signature;
    }

    public Map<String, Object> verifyToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token 为空");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("token 格式不正确");
        }
        String signingInput = parts[0] + "." + parts[1];
        String expected = base64Url(hmacSha256(signingInput));
        if (!constantTimeEquals(expected, parts[2])) {
            throw new IllegalArgumentException("token 签名无效");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Map<String, Object> payload = readJson(payloadJson);

        long exp = toLong(payload.get("exp"));
        long now = Instant.now().getEpochSecond();
        if (exp > 0 && now >= exp) {
            throw new IllegalArgumentException("token 已过期");
        }
        return payload;
    }

    private String json(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 序列化失败");
        }
    }

    private Map<String, Object> readJson(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("token 解析失败");
        }
    }

    private byte[] hmacSha256(String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("token 签名失败");
        }
    }

    private String base64Url(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private long toLong(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int r = 0;
        for (int i = 0; i < x.length; i++) {
            r |= x[i] ^ y[i];
        }
        return r == 0;
    }
}

