package com.college.student_service_platform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class WarningRecordService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public WarningRecordService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Long saveFromPythonResponse(Long userId, String studentNo, Long transcriptFileId, Object pythonResponse) {
        Map<String, Object> root = asMap(pythonResponse);
        Map<String, Object> data = asMap(root.get("data"));
        List<Object> courses = asList(data.get("courses"));
        Map<String, Object> report = asMap(data.get("report"));

        String warningLevel = asString(report.get("warning_level"));
        BigDecimal totalEarnedCredits = asBigDecimal(report.get("total_earned_credits"));

        List<Object> failedCourses = asList(report.get("failed_courses"));
        List<Object> missingCourses = asList(report.get("missing_core_courses"));
        List<Object> suggestions = asList(report.get("course_suggestions"));

        int courseCount = courses == null ? 0 : courses.size();
        int failedCourseCount = failedCourses == null ? 0 : failedCourses.size();
        int missingCourseCount = missingCourses == null ? 0 : missingCourses.size();

        Long id = System.currentTimeMillis();

        String sql = """
                INSERT INTO t_warning_record
                (id, user_id, student_no, transcript_file_id, training_plan_id, warning_level,
                 total_earned_credits, course_count, matched_course_count, failed_course_count, missing_course_count,
                 parsed_courses_json, matched_courses_json, failed_courses_json, missing_courses_json,
                 course_suggestions_json, training_plan_snapshot_json, analyze_status, remark, created_at, updated_at)
                VALUES
                (?, ?, ?, ?, ?, ?,
                 ?, ?, ?, ?, ?,
                 ?, ?, ?, ?,
                 ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                id,
                userId,
                studentNo,
                transcriptFileId,
                null,
                warningLevel == null || warningLevel.isBlank() ? "未知" : warningLevel,
                totalEarnedCredits,
                courseCount,
                0,
                failedCourseCount,
                missingCourseCount,
                toJson(courses),
                null,
                toJson(failedCourses),
                toJson(missingCourses),
                toJson(suggestions),
                null,
                1,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return id;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Object> asList(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        return null;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ignored) {
            return BigDecimal.ZERO;
        }
    }
}

