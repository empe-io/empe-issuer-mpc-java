package io.empe.mpc_issuer_verifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SchemaManagementConfig {

    @Bean
    @ConfigurationProperties(prefix = "issuer.api")
    @Validated
    public IssuerApiProperties issuerApiProperties() {
        return new IssuerApiProperties();
    }

    public static class IssuerApiProperties {
        private String baseUrl;

        private String clientSecret;

        // Default schema templates that can be configured in application properties
        private Map<String, SchemaTemplate> schemaTemplates = new HashMap<>();

        // Getters and setters
        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public Map<String, SchemaTemplate> getSchemaTemplates() {
            return schemaTemplates;
        }

        public void setSchemaTemplates(Map<String, SchemaTemplate> schemaTemplates) {
            this.schemaTemplates = schemaTemplates;
        }
    }

    public static class SchemaTemplate {
        private String name;

        private String type;

        private Map<String, SchemaProperty> properties = new HashMap<>();

        private String[] requiredFields = new String[0];

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, SchemaProperty> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, SchemaProperty> properties) {
            this.properties = properties;
        }

        public String[] getRequiredFields() {
            return requiredFields;
        }

        public void setRequiredFields(String[] requiredFields) {
            this.requiredFields = requiredFields;
        }
    }

    public static class SchemaProperty {
         private String type = "string";

        private String title;

        private String description;

        private String format;

        private boolean nullable = false;

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public Map<String, Object> toPropertyMap() {
            Map<String, Object> result = new HashMap<>();
            result.put("type", type);

            if (title != null) {
                result.put("title", title);
            }

            if (description != null) {
                result.put("description", description);
            }

            if (format != null) {
                result.put("format", format);
            }

            if (nullable) {
                result.put("nullable", true);
            }

            return result;
        }
    }
}