package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.CertificateApplyDetail;
import com.college.student_service_platform.dto.CertificateApplyItem;
import com.college.student_service_platform.dto.CertificateApplySubmitRequest;
import com.college.student_service_platform.dto.CertificateDecisionRequest;
import com.college.student_service_platform.service.AdminAuthService;
import com.college.student_service_platform.service.CertificateApplyService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificate")
public class CertificateApplyController {

    private final CertificateApplyService certificateApplyService;
    private final AdminAuthService adminAuthService;
    private final JdbcTemplate jdbcTemplate;

    public CertificateApplyController(CertificateApplyService certificateApplyService, AdminAuthService adminAuthService, JdbcTemplate jdbcTemplate) {
        this.certificateApplyService = certificateApplyService;
        this.adminAuthService = adminAuthService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/apply/submit")
    public Result<Long> submit(@RequestBody CertificateApplySubmitRequest request) {
        Long id = certificateApplyService.submit(request);
        return Result.success("提交成功", id);
    }

    @GetMapping("/apply/list")
    public Result<List<CertificateApplyItem>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return Result.success(certificateApplyService.list(status, keyword));
    }

    @GetMapping("/apply/{id}")
    public Result<CertificateApplyDetail> detail(@PathVariable("id") Long id) {
        return Result.success(certificateApplyService.detail(id));
    }

    @PostMapping("/apply/{id}/decision")
    public Result<Void> decision(
            @PathVariable("id") Long id,
            @RequestBody CertificateDecisionRequest request,
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
        Long approverId = jdbcTemplate.query(
                "SELECT id FROM t_user WHERE username = ? AND role_code = 'admin'",
                rs -> rs.next() ? rs.getLong("id") : null,
                username
        );
        certificateApplyService.decide(id, request, approverId, username);
        return Result.success("处理成功", null);
    }

    @DeleteMapping("/apply/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        certificateApplyService.delete(id);
        return Result.success();
    }
}
