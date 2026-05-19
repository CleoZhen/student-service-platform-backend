package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/party")
public class PartyController {

    private final JdbcTemplate jdbcTemplate;

    public PartyController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/progress")
    public Result<List<Map<String, Object>>> getProgress(@RequestParam("studentNo") String studentNo) {
        // 使用 \" 严格包裹别名，强制金仓数据库保留小驼峰格式，确保小程序能正确读取
        // 去掉了不兼容的 public. 前缀，直接使用表名
        String sql = "SELECT ps.stage_name AS \"stageName\", ss.start_date AS \"startDate\", ss.stage_status AS \"stageStatus\" " +
                "FROM t_student_stage ss " +
                "LEFT JOIN t_process_stage ps ON ss.stage_id = ps.stage_id " +
                "WHERE ss.student_no = ? ORDER BY ps.order_num ASC";

        List<Map<String, Object>> progressList = jdbcTemplate.queryForList(sql, studentNo);
        return Result.success("党团进度查询成功", progressList);
    }
}