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

    @Tool(description = "Create a new credential schema. Schemas define the structure and attributes of Verifiable Credentials in the SSI ecosystem. Each schema has a type, name, and defines the properties a credential can contain.")
    public Map<String, Object> createSchema(
            @ToolParam(description = "Human-readable name of the schema (e.g., 'ProofOfPurchase', 'IdentityCredential', 'EventTicket'). This should be clear and descriptive.") String name,
            @ToolParam(description = "Unique identifier for this schema type. Often matches the name but serves as the technical reference. Used when creating credential offerings.") String type,
            @ToolParam(description = "JSON map defining the schema properties. Each property should be a key-value pair where the key is the property name and the value is a map containing 'type' (string, number, boolean), 'title' (human-readable name), and optionally 'description' and 'format'. Example: {'ticketId': {'type': 'string', 'title': 'Ticket ID'}}") Map<String, Object> properties,
            @ToolParam(description = "List of field names that are mandatory in credentials using this schema. These must be keys that exist in the properties map. Example: ['name', 'issuanceDate']") List<String> requiredFields) {

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

    @Tool(description = "Retrieve all credential schemas available in the system. Returns a list of schema summaries including ID, name, type, version, and URI. Use this to explore existing schemas before creating new ones or when needing a schema ID for other operations.")
    public List<Map<String, Object>> getAllSchemas() {
        return restClient.get()
                .uri("/api/v1/schema")
                .retrieve()
                .body(List.class);
    }

    @Tool(description = "Retrieve detailed information about a specific schema by its unique identifier. Returns the complete schema definition including all properties and metadata.")
    public Map<String, Object> getSchemaById(
            @ToolParam(description = "Unique identifier (UUID) of the schema to retrieve. This is the 'id' field returned when creating a schema or listing all schemas. Format example: 'db5a33ae-2eef-41b4-9c74-2ed16c4bb4f4'") String id) {
        return restClient.get()
                .uri("/api/v1/schema/{id}", id)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Permanently delete a schema from the system. This operation cannot be undone. Note that deleting a schema will not affect credentials already issued using this schema.")
    public void deleteSchema(
            @ToolParam(description = "Unique identifier (UUID) of the schema to delete. This is the 'id' field returned when creating a schema or listing all schemas. Format example: 'db5a33ae-2eef-41b4-9c74-2ed16c4bb4f4'") String id) {
        restClient.delete()
                .uri("/api/v1/schema/{id}", id)
                .header("x-client-secret", clientSecret)
                .retrieve()
                .toBodilessEntity();
    }

    @Tool(description = "Check if a schema with a specific type already exists in the system. Returns true if at least one schema with the specified type exists, false otherwise. Useful before creating new schemas to avoid duplication.")
    public boolean schemaExistsByType(
            @ToolParam(description = "The schema type to check for existence. This is the unique identifier used when referencing the schema type in credential offerings. Case-sensitive.") String type) {
        List<Map<String, Object>> schemas = getAllSchemas();
        return schemas.stream()
                .anyMatch(schema -> type.equals(schema.get("type")));
    }

    @Tool(description = "Retrieve the most recent version of a schema by its type. When schemas evolve over time, new versions are created. This tool fetches the schema with the highest version number for a given type.")
    public Map<String, Object> getLatestSchemaByType(
            @ToolParam(description = "The schema type to search for. This returns the schema with the highest version number that matches this type. Case-sensitive.") String type) {
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