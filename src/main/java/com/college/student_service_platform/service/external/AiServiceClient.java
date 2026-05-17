package com.college.student_service_platform.service.external;

import com.college.student_service_platform.config.ExternalServiceProperties;
import com.college.student_service_platform.dto.AiAskRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final ExternalServiceProperties properties;

    public AiServiceClient(RestTemplate restTemplate, ExternalServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Object ask(AiAskRequest request) {
        String url = properties.getAiServiceBaseUrl() + "/api/student/ask";
        return restTemplate.postForObject(url, request, Object.class);
    }

    public Object ingestAll() {
        String url = properties.getAiServiceBaseUrl() + "/api/admin/ingest_all";
        return restTemplate.postForObject(url, null, Object.class);
    }
}