package com.college.student_service_platform.controller;

import com.college.student_service_platform.common.Result;
import com.college.student_service_platform.dto.AiAskRequest;
import com.college.student_service_platform.service.external.AiServiceClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AiProxyController {

    private final AiServiceClient aiServiceClient;

    public AiProxyController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @PostMapping("/student/ai/ask")
    public Result<Object> ask(@RequestBody AiAskRequest request) {
        Object response = aiServiceClient.ask(request);
        return Result.success("AI问答调用成功", response);
    }

    @PostMapping("/admin/ai/ingest-all")
    public Result<Object> ingestAll() {
        Object response = aiServiceClient.ingestAll();
        return Result.success("知识库更新调用成功", response);
    }
}