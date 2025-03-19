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

    @Tool(description = "Create a credential offering with flexible recipient options. This is the foundational method that can create both targeted (specific recipient) and open (anyone can claim) offerings. Most use cases should use the specialized createTargetedOffering or createOpenOffering methods instead.")
    public Map<String, Object> createOffering(
            @ToolParam(description = "The type of credential to offer, must match an existing schema type in the system. For example: 'ProofOfPurchase', 'EventTicket', 'MembershipCard'. Case-sensitive.") String type,
            @ToolParam(description = "The actual data to include in the credential, structured as key-value pairs. Keys must match the properties defined in the schema. For example, an EventTicket might include: {'ticketId': 'T12345', 'eventName': 'Spring Conference', 'seat': 'A12'}") Map<String, Object> credentialSubject,
            @ToolParam(description = "For targeted offerings, specify the recipient's DID (Decentralized Identifier). When provided, only the holder of this DID can claim the credential. For open offerings, leave this empty or null.") Optional<String> recipientDid) {

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

    @Tool(description = "Create a credential offering for a specific recipient. Targeted offerings can only be claimed by the holder of the specified DID, ensuring the credential is issued to exactly the intended recipient. This is appropriate for credentials that represent personal attributes, memberships, or specific authorizations.")
    public Map<String, Object> createTargetedOffering(
            @ToolParam(description = "The type of credential to offer, must match an existing schema type in the system. For example: 'ProofOfPurchase', 'IdentityCredential', 'MembershipCard'. Case-sensitive.") String type,
            @ToolParam(description = "The actual data to include in the credential, structured as key-value pairs. Keys must match the properties defined in the schema. Example: {'name': 'John Doe', 'membershipLevel': 'Premium', 'memberSince': '2023-01-15'}") Map<String, Object> credentialSubject,
            @ToolParam(description = "The Decentralized Identifier (DID) of the intended recipient. Only the wallet that can prove ownership of this DID will be able to claim the credential. Format example: 'did:empe:testnet:123abc456def'") String recipientDid) {

        return createOffering(type, credentialSubject, Optional.of(recipientDid));
    }

    @Tool(description = "Create an open credential offering that anyone can claim by scanning its QR code. Open offerings are ideal for general-purpose credentials like event attendance proofs, promotional coupons, or public certificates that don't need to be restricted to specific individuals.")
    public Map<String, Object> createOpenOffering(
            @ToolParam(description = "The type of credential to offer, must match an existing schema type in the system. Appropriate types for open offerings include: 'EventAttendance', 'PromotionalCoupon', 'PublicCertificate'. Case-sensitive.") String type,
            @ToolParam(description = "The actual data to include in the credential, structured as key-value pairs. Keys must match the properties defined in the schema. Example for an event attendance: {'eventName': 'Tech Conference 2025', 'location': 'San Francisco', 'date': '2025-06-15'}") Map<String, Object> credentialSubject) {

        return createOffering(type, credentialSubject, Optional.empty());
    }

  }