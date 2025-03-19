package io.empe.mpc_issuer_verifier;

import io.empe.mpc_issuer_verifier.service.CredentialIssuingService;
import io.empe.mpc_issuer_verifier.service.SchemaService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MpcIssuerVerifierApplication {

	public static void main(String[] args) {
		SpringApplication.run(MpcIssuerVerifierApplication.class, args);
	}


	@Bean
	public ToolCallbackProvider weatherTools(SchemaService schemaService, CredentialIssuingService issuerService) {
		return  MethodToolCallbackProvider.builder().toolObjects(schemaService, issuerService).build();
	}
}
