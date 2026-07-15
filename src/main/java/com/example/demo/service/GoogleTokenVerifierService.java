package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;

/**
 * Service chuyên trách xác thực Google Access Token.
 */
@Service
public class GoogleTokenVerifierService {

    public Map<String, Object> verifyToken(String accessToken) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Google Access Token không hợp lệ hoặc đã hết hạn.");
        }
    }
}
