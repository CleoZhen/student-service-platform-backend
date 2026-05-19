package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/notice")
public class NoticeController {

    private final JdbcTemplate jdbcTemplate;

    public NoticeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList(@RequestParam("tag") String tag) {
        // 左连接 t_file 获取附件信息
        String sql = "SELECT n.id AS notificationId, n.title, n.content, n.tags, n.is_urgent AS isUrgent, " +
                "n.created_at AS createdAt, f.id AS fileRecordId, f.original_name AS fileOriginalName " +
                "FROM t_notification n " +
                "LEFT JOIN t_file f ON n.file_id = f.id " +
                "WHERE n.tags LIKE ? " +
                "ORDER BY n.is_urgent DESC, n.created_at DESC";

        List<Map<String, Object>> notices = jdbcTemplate.queryForList(sql, "%" + tag + "%");
        return Result.success("精准通知拉取成功", notices);
    }

    @PostMapping("/confirm")
    public Result<Void> confirmNotice(
            @RequestParam("notificationId") Long notificationId,
            @RequestParam("studentNo") String studentNo
    ) {
        // 写入已读回执。由于脚本中 id 没有自增序列，这里暂用时间戳防重复
        String sql = "INSERT INTO t_notification_receipt (id, notification_id, student_no, is_confirmed, confirmed_at) " +
                "VALUES (?, ?, ?, true, CURRENT_TIMESTAMP)";

        long mockId = System.currentTimeMillis();
        int rows = jdbcTemplate.update(sql, mockId, notificationId, studentNo);
        return rows > 0 ? Result.success() : Result.fail("回执确认失败");
    }
}