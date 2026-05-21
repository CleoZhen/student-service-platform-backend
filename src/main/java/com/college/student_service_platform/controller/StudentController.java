package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.StudentImportRequest;
import com.college.student_service_platform.dto.StudentImportResult;
import com.college.student_service_platform.dto.StudentListItem;
import com.college.student_service_platform.service.StudentService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final JdbcTemplate jdbcTemplate;
    private final StudentService studentService;

    public StudentController(JdbcTemplate jdbcTemplate, StudentService studentService) {
        this.jdbcTemplate = jdbcTemplate;
        this.studentService = studentService;
    }

    @PostMapping("/import")
    public Result<StudentImportResult> importStudents(@RequestBody StudentImportRequest request) {
        StudentImportResult result = studentService.importStudents(request == null ? null : request.getStudents());
        return Result.success("导入成功", result);
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
            // 1. 查询你新建的 t_student 表，获取真实姓名、专业和年级
            String sqlStudent = "SELECT name, major, grade FROM t_student WHERE student_no = ?";
            List<Map<String, Object>> students = jdbcTemplate.queryForList(sqlStudent, account);

            Map<String, Object> userInfo = new HashMap<>();
            if (!students.isEmpty()) {
                // 如果在表里找到了这个学生
                Map<String, Object> stu = students.get(0);
                userInfo.put("name", stu.get("name"));
                userInfo.put("major", stu.get("major"));
                userInfo.put("grade", stu.get("grade"));
            } else {
                // 容错处理：如果查不到，给个默认值
                userInfo.put("name", "未录入");
                userInfo.put("major", "未分配专业");
                userInfo.put("grade", "未知年级");
            }
            responseData.put("userInfo", userInfo);

            // 2. 课程列表 (以后有了成绩表，再把这里的假数据换成查数据库的真实 SQL)
            List<Map<String, Object>> courses = new ArrayList<>();
            Map<String, Object> c1 = new HashMap<>(); c1.put("name", "高等数学Ⅰ"); c1.put("credits", 5);
            Map<String, Object> c2 = new HashMap<>(); c2.put("name", "Java程序设计"); c2.put("credits", 4);
            Map<String, Object> c3 = new HashMap<>(); c3.put("name", "数据库原理"); c3.put("credits", 3);
            courses.add(c1);
            courses.add(c2);
            courses.add(c3);
            responseData.put("courses", courses);

            // 3. 学分和绩点统计 (以后也可以通过 SQL 从成绩表动态聚合 SUM 和 AVG)
            responseData.put("totalCredits", 12);
            responseData.put("gpa", 3.92);

            return Result.success("获取信息成功", responseData);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("查询学生信息失败: " + e.getMessage());
        }
    }
}
