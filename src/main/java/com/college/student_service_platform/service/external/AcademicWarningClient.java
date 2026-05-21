package com.college.student_service_platform.service.external;

import com.college.student_service_platform.config.ExternalServiceProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AcademicWarningClient {

    private final RestTemplate restTemplate;
    private final ExternalServiceProperties properties;

    public AcademicWarningClient(RestTemplate restTemplate, ExternalServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Object getTrainingPlan() {
        String url = properties.getWarningServiceBaseUrl() + "/api/admin/warning/training_plan";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object saveTrainingPlan(Object request) {
        String url = properties.getWarningServiceBaseUrl() + "/api/admin/warning/training_plan";
        return restTemplate.postForObject(url, request, Object.class);
    }

    public Object analyzeTranscript(MultipartFile file, String studentNo, String trainingPlanJson) throws IOException {
        String url = properties.getWarningServiceBaseUrl() + "/api/student/warning/analyze";

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("studentNo", studentNo);
        if (trainingPlanJson != null && !trainingPlanJson.isBlank()) {
            body.add("training_plan", trainingPlanJson);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Object> response = restTemplate.postForEntity(url, requestEntity, Object.class);
        return response.getBody();
    }
}