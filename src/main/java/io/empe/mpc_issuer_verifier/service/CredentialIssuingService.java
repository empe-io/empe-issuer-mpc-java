package io.empe.mpc_issuer_verifier.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class CredentialIssuingService {

    private final RestClient restClient;
    private final String clientSecret;
    private final String baseUrl;

    public CredentialIssuingService(
            @Value("${issuer.api.base-url:http://example.com/api/v1}") String baseUrl,
            @Value("${issuer.api.client-secret:}") String clientSecret) {
        this.baseUrl = baseUrl;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Tool(description = "Create a credential offering")
    public Map<String, Object> createOffering(
            @ToolParam(description = "credential_type") String type,
            @ToolParam(description = "Credential subject data as a map") Map<String, Object> credentialSubject,
            @ToolParam(description = "Optional recipient DID for targeted offerings") Optional<String> recipientDid) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("credential_type", type);
        requestBody.put("credential_subject", credentialSubject);

        recipientDid.ifPresent(did -> requestBody.put("recipient", did));

        return restClient.post()
                .uri("/api/v1/offering")
                .header("x-client-secret", clientSecret)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Create a targeted credential offering (with specified recipient)")
    public Map<String, Object> createTargetedOffering(
            @ToolParam(description = "Credential type (must match a schema type)") String type,
            @ToolParam(description = "Credential subject data as a map") Map<String, Object> credentialSubject,
            @ToolParam(description = "Recipient DID") String recipientDid) {

        return createOffering(type, credentialSubject, Optional.of(recipientDid));
    }

    @Tool(description = "Create an open credential offering (available to anyone)")
    public Map<String, Object> createOpenOffering(
            @ToolParam(description = "Credential type (must match a schema type)") String type,
            @ToolParam(description = "Credential subject data as a map") Map<String, Object> credentialSubject) {

        return createOffering(type, credentialSubject, Optional.empty());
    }

    @Tool(description = "Initiate DID authentication")
    public Map<String, Object> initiateDIDAuthentication(
            @ToolParam(description = "Recipient DID to authenticate") String recipientDid) {

        Map<String, Object> requestBody = Map.of(
                "did", recipientDid
        );

        return restClient.post()
                .uri("/api/v1/authorize")
                .header("x-client-secret", clientSecret)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Verify DID authentication")
    public Map<String, Object> verifyDIDAuthentication(
            @ToolParam(description = "Challenge from initiate step") String challenge,
            @ToolParam(description = "Signed challenge response") String signedChallenge) {

        Map<String, Object> requestBody = Map.of(
                "challenge", challenge,
                "signedChallenge", signedChallenge
        );

        return restClient.post()
                .uri("/api/v1/authorize/verify")
                .header("x-client-secret", clientSecret)
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Exchange authorization code for access token")
    public Map<String, Object> exchangeToken(
            @ToolParam(description = "Authorization code from verification step") String authorizationCode) {

        Map<String, Object> requestBody = Map.of(
                "authorization_code", authorizationCode
        );

        return restClient.post()
                .uri("/api/v1/connect/token")
                .body(requestBody)
                .retrieve()
                .body(Map.class);
    }

    @Tool(description = "Issue credential (requires access token)")
    public Map<String, Object> issueCredential(
            @ToolParam(description = "Offering ID") String offeringId,
            @ToolParam(description = "Access token from token exchange") String accessToken) {

        return restClient.post()
                .uri("/api/v1/issue-credential/{id}", offeringId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
    }
}