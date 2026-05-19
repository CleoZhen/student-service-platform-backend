package com.college.student_service_platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExternalServiceProperties {

    @Value("${external.ai-service.base-url}")
    private String aiServiceBaseUrl;

    @Value("${external.warning-service.base-url}")
    private String warningServiceBaseUrl;

    public String getAiServiceBaseUrl() {
        return aiServiceBaseUrl;
    }

    public String getWarningServiceBaseUrl() {
        return warningServiceBaseUrl;
    }
}