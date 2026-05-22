package com.placementpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model.name:gemini-2.0-flash-lite}")
    private String modelName;

    private static final List<String> FALLBACK_TIPS = List.of(
        "🌟 Career Strategy: Focus on building robust REST APIs with Spring Boot and structuring complex MySQL schemas. Practice explaining normalization, indexes, and transactional integrity during interviews!",
        "💡 Interview Insight: Recruiter dashboards prioritize students who demonstrate hands-on projects combining a robust Java/Spring backend with responsive modern CSS/JavaScript. Make sure to detail your contributions clearly!",
        "🚀 Technical Edge: Strengthen your data structures and algorithms foundation. Top product companies frequently evaluate candidates on array manipulation, SQL query optimization, and dynamic programming.",
        "🎓 Placement Tip: Elevate your resume by highlighting your experience with role-based dashboard security, database trigger events, and automated notifications in real-world environments!"
    );

    public String getAIAdvice(String prompt) {
        try {
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + modelName + ":generateContent?key=" + apiKey;

            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", Collections.singletonList(part));
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    apiUrl, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> contentResult = (Map<String, Object>) candidates.get(0).get("content");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentResult.get("parts");
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            // Fall back to a highly professional, inspiring career advisor tip if quota is exhausted
            System.err.println("[AI ADVISOR] Quota exhausted or error occurred: " + e.getMessage());
            int index = (int) (Math.random() * FALLBACK_TIPS.size());
            return FALLBACK_TIPS.get(index);
        }
        int index = (int) (Math.random() * FALLBACK_TIPS.size());
        return FALLBACK_TIPS.get(index);
    }
}
