package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.StudentImportRequest;
import com.college.student_service_platform.dto.StudentImportResult;
import com.college.student_service_platform.dto.StudentListItem;
import com.college.student_service_platform.service.StudentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final JdbcTemplate jdbcTemplate;
    private final StudentService studentService;
    private final ObjectMapper objectMapper;

    public StudentController(JdbcTemplate jdbcTemplate, StudentService studentService, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.studentService = studentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/import")
    public Result<StudentImportResult> importStudents(@RequestBody StudentImportRequest request) {
        StudentImportResult result = studentService.importStudents(request == null ? null : request.getStudents());
        return Result.success("Import succeeded", result);
    }

    @GetMapping("/list")
    public Result<List<StudentListItem>> listStudents(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(studentService.listStudents(keyword));
    }

    @DeleteMapping("/{studentNo}")
    public Result<Void> deleteStudent(@PathVariable String studentNo) {
        studentService.deleteByStudentNo(studentNo);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getStudentInfo(@RequestParam("account") String account) {
        Map<String, Object> responseData = new HashMap<>();

        try {
            String sqlStudent = "SELECT name, major, grade FROM t_student WHERE student_no = ?";
            List<Map<String, Object>> students = jdbcTemplate.queryForList(sqlStudent, account);

            Map<String, Object> userInfo = new HashMap<>();
            if (!students.isEmpty()) {
                Map<String, Object> stu = students.get(0);
                userInfo.put("name", stu.get("name"));
                userInfo.put("major", stu.get("major"));
                userInfo.put("grade", stu.get("grade"));
            } else {
                userInfo.put("name", "\u672a\u5f55\u5165");
                userInfo.put("major", "\u672a\u5206\u914d\u4e13\u4e1a");
                userInfo.put("grade", "\u672a\u77e5\u5e74\u7ea7");
            }
            responseData.put("userInfo", userInfo);

            List<Map<String, Object>> courses = new ArrayList<>();
            double totalCredits = 0.0;
            double gpa = 0.0;

            try {
                String sqlWarning = """
                        SELECT parsed_courses_json, total_earned_credits, official_gpa
                        FROM t_warning_record
                        WHERE student_no = ?
                        ORDER BY created_at DESC
                        LIMIT 1
                        """;
                List<Map<String, Object>> warningRecords = jdbcTemplate.queryForList(sqlWarning, account);

                if (!warningRecords.isEmpty()) {
                    Map<String, Object> record = warningRecords.get(0);
                    String coursesJson = record.get("parsed_courses_json") == null
                            ? null
                            : String.valueOf(record.get("parsed_courses_json"));

                    if (coursesJson != null && !coursesJson.isBlank()) {
                        courses = parseCourses(coursesJson);
                    }

                    Object creditsObj = record.get("total_earned_credits");
                    if (creditsObj != null) {
                        totalCredits = Double.parseDouble(creditsObj.toString());
                    }

                    Object gpaObj = record.get("official_gpa");
                    if (gpaObj != null) {
                        gpa = Double.parseDouble(gpaObj.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            responseData.put("courses", courses);
            responseData.put("totalCredits", totalCredits);
            responseData.put("gpa", gpa);

            return Result.success("Student info loaded", responseData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Failed to query student info: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseCourses(String coursesJson) throws Exception {
        try {
            return objectMapper.readValue(coursesJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            Map<String, Object> reportData = objectMapper.readValue(coursesJson, new TypeReference<Map<String, Object>>() {});
            Object coursesObj = reportData.get("courses");
            if (coursesObj instanceof List<?>) {
                return (List<Map<String, Object>>) coursesObj;
            }
            return new ArrayList<>();
        }
    }
}
