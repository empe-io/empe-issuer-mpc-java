package io.empe.mpc_issuer_verifier.controller;

import io.empe.mpc_issuer_verifier.service.CredentialIssuingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/validator")
public class ValidatorCredentialController {

    private final CredentialIssuingService credentialIssuingService;

    public ValidatorCredentialController(CredentialIssuingService credentialIssuingService) {
        this.credentialIssuingService = credentialIssuingService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createValidatorCredential(@RequestBody Map<String, String> request) {
        String validatorAddress = request.get("validatorAddress");
        
        if (validatorAddress == null || validatorAddress.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Validator address is required"
            ));
        }
        
        // Create validator credential with QR code
        Map<String, Object> response = credentialIssuingService.createValidatorCredentialWithQR(validatorAddress);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/qr/{offeringId}")
    public ResponseEntity<Map<String, Object>> getQrCodeForOffering(@PathVariable String offeringId) {
        // You would need to implement this method to retrieve QR codes for existing offerings
        // This is a placeholder for future implementation
        return ResponseEntity.ok(Map.of(
            "message", "This endpoint would retrieve a QR code for offering ID: " + offeringId
        ));
    }
}