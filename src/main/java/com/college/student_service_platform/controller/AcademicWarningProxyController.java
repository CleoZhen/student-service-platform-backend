package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.service.WarningRecordService;
import com.college.student_service_platform.service.external.AcademicWarningClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AcademicWarningProxyController {

    private final AcademicWarningClient academicWarningClient;
    private final WarningRecordService warningRecordService;

    public AcademicWarningProxyController(
            AcademicWarningClient academicWarningClient,
            WarningRecordService warningRecordService
    ) {
        this.academicWarningClient = academicWarningClient;
        this.warningRecordService = warningRecordService;
    }

    @GetMapping("/admin/warning/training-plan")
    public Result<Object> getTrainingPlan() {
        Object response = academicWarningClient.getTrainingPlan();
        return Result.success("培养方案查询成功", response);
    }

    @PostMapping("/admin/warning/training-plan")
    public Result<Object> saveTrainingPlan(@RequestBody Object request) {
        Object response = academicWarningClient.saveTrainingPlan(request);
        return Result.success("培养方案保存成功", response);
    }

    @PostMapping("/student/warning/analyze")
    public Result<Object> analyzeTranscript(
            @RequestParam("file") MultipartFile file,
            @RequestParam("studentNo") String studentNo
    ) throws IOException {
        Object response = academicWarningClient.analyzeTranscript(file, studentNo);
        try {
            warningRecordService.saveFromPythonResponse(null, studentNo, null, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success("学业预警分析成功", response);
    }
}
