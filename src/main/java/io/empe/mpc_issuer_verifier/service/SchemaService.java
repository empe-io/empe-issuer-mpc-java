package io.empe.mpc_issuer_verifier.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class SchemaService {

    private final RestClient restClient;
    private final String clientSecret;
    private final String baseUrl;

    public SchemaService(
            @Value("${issuer.api.base-url:http://example.com/api/v1}") String baseUrl,
            @Value("${issuer.api.client-secret:}") String clientSecret) {
        this.baseUrl = baseUrl;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Tool(description = "Create a new credential schema")
    public Map<String, Object> createSchema(
            @ToolParam(description = "Schema name") String name,
            @ToolParam(description = "Schema type") String type,
            @ToolParam(description = "Schema properties as JSON") Map<String, Object> properties,
            @ToolParam(description = "Required fields (array of field names)") List<String> requiredFields) {

        // Construct the schema structure based on API requirements
        Map<String, Object> credentialSubject = Map.of(
                "type", "object",
                "properties", properties,
                "required", requiredFields
        );

        Map<String, Object> requestBody = Map.of(
                "name", name,
                "type", type,
                "credentialSubject", credentialSubject
        );

        return restClient.post()
                .uri("/api/v1/schema")
                .header("x-client-secret", clientSecret)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Get all schemas")
    public List<Map<String, Object>> getAllSchemas() {
        return restClient.get()
                .uri("/api/v1/schema")
                .retrieve()
                .body(List.class);
    }

    @Tool(description = "Get schema by ID")
    public Map<String, Object> getSchemaById(
            @ToolParam(description = "Schema ID") String id) {
        return restClient.get()
                .uri("/api/v1/schema/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Delete schema by ID")
    public void deleteSchema(
            @ToolParam(description = "Schema ID") String id) {
        restClient.delete()
                .uri("/api/v1/schema/{id}", id)
                .header("x-client-secret", clientSecret)
                .retrieve()
                .toBodilessEntity();
    }

    @Tool(description = "Check if schema exists by type")
    public boolean schemaExistsByType(
            @ToolParam(description = "Schema type") String type) {
        List<Map<String, Object>> schemas = getAllSchemas();
        return schemas.stream()
                .anyMatch(schema -> type.equals(schema.get("type")));
    }

    @Tool(description = "Get latest schema version by type")
    public Map<String, Object> getLatestSchemaByType(
            @ToolParam(description = "Schema type") String type) {
        List<Map<String, Object>> schemas = getAllSchemas();
        return schemas.stream()
                .filter(schema -> type.equals(schema.get("type")))
                .max((s1, s2) -> {
                    Integer v1 = (Integer) s1.get("version");
                    Integer v2 = (Integer) s2.get("version");
                    return v1.compareTo(v2);
                })
                .orElse(null);
    }
}