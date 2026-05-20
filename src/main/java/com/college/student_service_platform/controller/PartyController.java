package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
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
        Integer partyStageId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(party_stage_id, 0) FROM t_student WHERE student_no = ?",
                Integer.class,
                studentNo
        );
        int currentStageId = partyStageId == null ? 0 : partyStageId;

        List<Map<String, Object>> stages = jdbcTemplate.queryForList(
                "SELECT stage_id, stage_name, order_num FROM t_process_stage ORDER BY order_num ASC"
        );

        List<Map<String, Object>> progressList = new ArrayList<>();

        Map<String, Object> notApplied = new HashMap<>();
        notApplied.put("stageName", "未申请");
        notApplied.put("startDate", null);
        if (currentStageId == 0) {
            notApplied.put("stageStatus", "进行中");
        } else {
            notApplied.put("stageStatus", "已完成");
        }
        progressList.add(notApplied);

        for (Map<String, Object> s : stages) {
            Object stageIdObj = s.get("stage_id");
            if (!(stageIdObj instanceof Number)) {
                continue;
            }
            int stageId = ((Number) stageIdObj).intValue();
            String stageName = s.get("stage_name") == null ? "" : String.valueOf(s.get("stage_name"));

            Map<String, Object> row = new HashMap<>();
            row.put("stageName", stageName);
            row.put("startDate", null);

            if (currentStageId <= 0) {
                row.put("stageStatus", "未开始");
            } else if (stageId < currentStageId) {
                row.put("stageStatus", "已完成");
            } else if (stageId == currentStageId) {
                row.put("stageStatus", "进行中");
            } else {
                row.put("stageStatus", "未开始");
            }

            progressList.add(row);
        }

        return Result.success("党团进度查询成功", progressList);
    }
}
