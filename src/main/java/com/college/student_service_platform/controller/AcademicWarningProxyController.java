package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.FileUploadResponse;
import com.college.student_service_platform.service.FileService;
import com.college.student_service_platform.service.UserLookupService;
import com.college.student_service_platform.service.WarningRecordService;
import com.college.student_service_platform.service.external.AcademicWarningClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AcademicWarningProxyController {

    private final AcademicWarningClient academicWarningClient;
    private final FileService fileService;
    private final UserLookupService userLookupService;
    private final WarningRecordService warningRecordService;

    public AcademicWarningProxyController(
            AcademicWarningClient academicWarningClient,
            FileService fileService,
            UserLookupService userLookupService,
            WarningRecordService warningRecordService
    ) {
        this.academicWarningClient = academicWarningClient;
        this.fileService = fileService;
        this.userLookupService = userLookupService;
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
        Long userId = userLookupService.findUserIdByStudentNo(studentNo);
        FileUploadResponse uploaded = fileService.uploadFile(file, "transcript", userId);

        Object response = academicWarningClient.analyzeTranscript(file, studentNo);
        Long recordId = warningRecordService.saveFromPythonResponse(userId, studentNo, uploaded.getId(), response);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("warningRecordId", recordId);
        data.put("pythonResponse", response);
        data.put("file", uploaded);

        return Result.success("学业预警分析成功", data);
    }
}
