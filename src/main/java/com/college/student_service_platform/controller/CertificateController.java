package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.CertificateApplySubmitRequest;
import com.college.student_service_platform.service.CertificateApplyService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/student/certificate", "/api/student/cert"})
public class CertificateController {

    private final JdbcTemplate jdbcTemplate;
    private final CertificateApplyService certificateApplyService;

    public CertificateController(JdbcTemplate jdbcTemplate, CertificateApplyService certificateApplyService) {
        this.jdbcTemplate = jdbcTemplate;
        this.certificateApplyService = certificateApplyService;
    }

    @PostMapping("/apply")
    public Result<Long> apply(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(value = "studentNo", required = false) String studentNoParam,
            @RequestParam(value = "certificateType", required = false) String certificateTypeParam,
            @RequestParam(value = "extraData", required = false) String extraDataParam
    ) {
        String studentNo = pickFirstNonBlank(studentNoParam, bodyValue(requestBody, "studentNo"));
        String certificateType = pickFirstNonBlank(certificateTypeParam, bodyValue(requestBody, "certificateType"));
        String extraData = pickFirstNonBlank(extraDataParam, bodyValue(requestBody, "extraData"));

        if (!StringUtils.hasText(studentNo) || !StringUtils.hasText(certificateType)) {
            return Result.fail("studentNo、certificateType 不能为空");
        }

        CertificateApplySubmitRequest request = new CertificateApplySubmitRequest();
        request.setStudentNo(studentNo);
        request.setCertificateType(certificateType);
        request.setExtraData(extraData);
        Long applyId = certificateApplyService.submit(request);
        return Result.success("证明申请提交成功", applyId);
    }

    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory(@RequestParam("studentNo") String studentNo) {
        String sql = "SELECT id, student_no AS \"studentNo\", certificate_type AS \"certificateType\", " +
                "apply_status AS \"applyStatus\", extra_data AS \"extraData\", file_id AS \"fileId\", " +
                "TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') AS \"createdAt\" " +
                "FROM t_certificate_apply WHERE student_no = ? ORDER BY created_at DESC";

        List<Map<String, Object>> history = jdbcTemplate.queryForList(sql, studentNo);
        return Result.success("申请历史拉取成功", history);
    }

    private String bodyValue(Map<String, Object> requestBody, String key) {
        if (requestBody == null) {
            return null;
        }
        Object value = requestBody.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private String pickFirstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
