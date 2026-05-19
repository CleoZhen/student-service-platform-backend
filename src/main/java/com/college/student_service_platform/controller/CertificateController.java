package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/certificate")
public class CertificateController {

    private final JdbcTemplate jdbcTemplate;

    public CertificateController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/apply")
    public Result<Void> apply(
            @RequestParam("studentNo") String studentNo,
            @RequestParam("certificateType") String certificateType,
            @RequestParam("extraData") String extraData
    ) {
        // 初始申请的 apply_status 设定为 '待审核'
        String sql = "INSERT INTO t_certificate_apply (id, student_no, certificate_type, apply_status, extra_data) " +
                "VALUES (?, ?, ?, '待审核', ?)";

        long mockId = System.currentTimeMillis();
        int rows = jdbcTemplate.update(sql, mockId, studentNo, certificateType, extraData);
        return rows > 0 ? Result.success("证明申请提交成功", null) : Result.fail("申请提交失败");
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(@RequestParam("studentNo") String studentNo) {
        // 映射查询记录，严格转换 apply_status
        String sql = "SELECT id, student_no AS studentNo, certificate_type AS certificateType, " +
                "apply_status AS applyStatus, extra_data AS extraData, created_at AS createdAt " +
                "FROM t_certificate_apply WHERE student_no = ? ORDER BY created_at DESC";

        List<Map<String, Object>> history = jdbcTemplate.queryForList(sql, studentNo);
        return Result.success("申请历史拉取成功", history);
    }
}