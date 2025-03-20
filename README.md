# AI Agent Identity and Verifiable Data

## Introduction

End-to-End Verifiable Data Infrastructure (EVDI) is a comprehensive infrastructure for managing digital identity and credentials in AI agent ecosystems. EVDI provides a secure, decentralized framework that enables AI agents to establish, manage, and verify their identities while interacting with other agents, humans and systems.

### What is Self-Sovereign Identity (SSI)?

Self-Sovereign Identity (SSI) is a model for digital identity management that gives individuals (and in our case, AI agents) control over their own identities and data. Unlike traditional centralized identity systems where a single authority controls and verifies identities, SSI allows entities to:

1. **Own their identity**: Control their digital presence without depending on any central authority
2. **Manage their credentials**: Store and share verifiable credentials as needed
3. **Prove claims**: Demonstrate attributes about themselves without revealing unnecessary information
4. **Establish trust**: Create trusted connections with other entities in a decentralized manner

### The Role of Issuers and Verifiers

In the SSI ecosystem, there are three key roles:

- **Issuers**: Entities that create and sign verifiable credentials (e.g., a university issuing a degree)
- **Holders**: Entities that receive and store credentials (e.g., an AI agent storing its capabilities)
- **Verifiers**: Entities that request and verify credentials (e.g., a service checking an agent's authorization)

This repository focuses on the **Issuer** component, which allows trusted authorities to create and issue verifiable credentials to AI agents or other entities.

### Why EVDI Matters for AI Agents

As AI systems become more autonomous and interact with various entities, they need secure ways to:

- Prove their identity and capabilities
- Establish trust with humans, other AI agents, and external systems
- Share verifiable information without compromising security
- Maintain privacy while engaging in complex interactions

EVDI enables these capabilities through cryptographically secure, tamper-evident credentials that can be verified without contacting the original issuer.

### Scope of Interactions

The interactions enabled by EVDI are not limited to agent-to-agent communications. They extend to:

- **Agent-to-Human**: AI agents can present and issue verifiable credentials to humans
- **Agent-to-System**: AI agents can authenticate with external systems using their credentials
- **Human-to-Agent**: Humans can present credentials to AI systems
- **System-to-Agent**: External systems can issue credentials to AI agents

For more information, visit our [documentation](https://docs.empe.io/evdi/intro).

This repository contains the MCP (Model Context Protocol) Server for the Empe Self-Sovereign Identity Issuer Service, which is the first component of the broader EVDI ecosystem. Future components will include credential verification capabilities, agent-to-agent authentication, and self-sovereign identity management for AI agents.

## MCP Server for SSI Issuer Service

### Overview

This project implements a Model Context Protocol (MCP) server that acts as an adapter between AI models and the EMPE Issuer Service within the Self-Sovereign Identity (SSI) ecosystem. Built on Spring Boot and Spring AI's MCP server capabilities, it allows AI assistants to interact with Issuer Service functionality through well-defined tools, enabling AI-driven credential management workflows.

## What is MCP?

Model Context Protocol (MCP) is a protocol that enables AI models to interact with external systems and tools. It allows models to:

- Discover available tools
- Make tool calls to perform specific actions
- Process the results from those actions

This MCP server exposes Issuer Service functionality as tools that can be called by AI models, bridging the gap between natural language interactions and SSI credential operations.

## How it Works

1. **Tool Registration**: The server defines SSI operations as tools with clear descriptions and parameters
2. **AI Integration**: AI models connect to the MCP server to discover available tools
3. **Tool Execution**: When the AI needs to perform an SSI operation, it makes a tool call to this server
4. **Result Processing**: The server executes the operation on the Issuer Service and returns results to the AI

## Key Components

- **MCP Server**: Foundation for exposing tools to AI models
- **Schema Management Tools**: Capabilities for creating and managing credential schemas
- **Credential Issuance Tools**: Functions for generating and issuing verifiable credentials
- **Issuer Service Client**: Handles communication with the underlying Issuer Service API

## Future Capabilities

The EVDI ecosystem is continuously evolving with planned features including:

1. **Credential Verification**: Tools for verifying credentials presented by agents
2. **Agent Identity Management**: Enabling AI agents to manage their own identities with DIDs
3. **Credential Storage**: Allowing agents to collect and store their own credentials
4. **Selective Disclosure**: Enabling agents to share specific credential attributes without revealing all information
5. **Agent-to-Agent Authentication**: Secure authentication mechanisms between AI agents using DIDs

## Accessing the Issuer Service

### One-Click Portal
We provide a one-click portal where you can easily deploy your own instance of the Issuer or Verifier service. You can access the portal at <https://oneclick.empe.io/>.

### Demo Environment
For testing purposes, we provide a demo environment at:
```
https://mcp-demo-issuer.evdi.app
```

Note: Access to the demo environment requires an access code which is not included in this README for security reasons. The access code will be provided through secure channels to interested users.

## Setting Up the MCP Server

### Prerequisites
- Java 23 or later
- Maven 3.8+
- Access to an EMPE Issuer Service instance

### Configuration

Configure the MCP server in your `application.properties` or `application.yml`:

```yaml
# MCP Server Configuration
spring.ai.mcp.server:
  port: 8090
  cors.allowed-origins: "*"  # For development only

# Issuer Service Connection
issuer:
  api:
    base-url: https://your-issuer-service.example.com/api/v1
    client-secret: your-client-secret
    schema-templates:
      proof-of-purchase:
        name: ProofOfPurchase
        type: ProofOfPurchase
        properties:
          ticket:
            type: string
            title: Ticket Number
          seat:
            type: string
            title: Seat Assignment
          description:
            type: string
            title: Event Description
        required-fields:
          - ticket
          - seat
          - description
```

### Build and Run
```bash
mvn clean package
java -jar target/mpc-issuer-verifier-0.0.1-SNAPSHOT.jar
```

## Connecting AI Models to the MCP Server

### OpenAI Custom GPT Example
If you're creating a Custom GPT with OpenAI:

1. Go to GPT Builder
2. In the "Configure" tab, select "Actions"
3. Add a new action with:
   - Authentication: None
   - API endpoint: `http://your-mcp-server:8090/api/v1`
   - Schema: Set to "Import from URL" and use `http://your-mcp-server:8090/api/v1/openapi.json`

### Anthropic Claude Example
For Claude, you would use the function calling capabilities:

```javascript
const response = await client.messages.create({
  model: "claude-3-opus-20240229",
  max_tokens: 1000,
  system: "You are an assistant that helps users manage SSI credentials.",
  messages: [
    {
      role: "user",
      content: "Create a proof of purchase credential"
    }
  ],
  tools: [
    {
      name: "mcp_tools",
      url: "http://your-mcp-server:8090/api/v1/openapi.json"
    }
  ]
});
```

## Available AI Tools

The MCP server exposes the following tools to AI models with detailed descriptions to guide appropriate use:

### Schema Management Tools

#### createSchema
Creates a new credential schema defining the structure for verifiable credentials.

**Parameters:**
- **name**: Human-readable name (e.g., 'ProofOfPurchase', 'IdentityCredential')
- **type**: Unique identifier for this schema type, used when creating offerings
- **properties**: JSON map defining schema properties with their types and descriptions
- **requiredFields**: List of mandatory fields that must be included in credentials

#### getAllSchemas
Retrieves all credential schemas in the system.

#### getSchemaById
Fetches a specific schema by its unique identifier (UUID).

**Parameters:**
- **id**: UUID of the schema to retrieve (e.g., 'db5a33ae-2eef-41b4-9c74-2ed16c4bb4f4')

#### deleteSchema
Permanently removes a schema from the system.

**Parameters:**
- **id**: UUID of the schema to delete

#### schemaExistsByType
Checks if a schema with the specified type already exists.

**Parameters:**
- **type**: Schema type to check (case-sensitive)

#### getLatestSchemaByType
Retrieves the most recent version of a schema by its type.

**Parameters:**
- **type**: Schema type to search for (case-sensitive)

### Credential Issuance Tools

#### createTargetedOffering
Creates a credential offering for a specific recipient DID.

**Parameters:**
- **type**: Type of credential to offer (must match an existing schema)
- **credentialSubject**: Data to include in the credential as key-value pairs
- **recipientDid**: DID of the intended recipient (e.g., 'did:empe:testnet:123abc456def')

#### createOpenOffering
Creates an open credential offering that anyone can claim.

**Parameters:**
- **type**: Type of credential to offer (must match an existing schema)
- **credentialSubject**: Data to include in the credential as key-value pairs

## Example AI Interactions

### Example 1: Creating a Schema and Offering a Credential

**User**: "Create a membership credential for our premium members."

**AI Processing**:
1. First, check if a suitable schema exists:
   ```json
   {
     "name": "schemaExistsByType",
     "parameters": {
       "type": "MembershipCredential"
     }
   }
   ```

2. If no schema exists, create one:
   ```json
   {
     "name": "createSchema",
     "parameters": {
       "name": "MembershipCredential",
       "type": "MembershipCredential",
       "properties": {
         "memberName": {
           "type": "string",
           "title": "Member Name"
         },
         "membershipLevel": {
           "type": "string", 
           "title": "Membership Level"
         },
         "validUntil": {
           "type": "string",
           "title": "Valid Until",
           "format": "date"
         }
       },
       "requiredFields": ["memberName", "membershipLevel", "validUntil"]
     }
   }
   ```

3. Create a credential offering:
   ```json
   {
     "name": "createOpenOffering",
     "parameters": {
       "type": "MembershipCredential",
       "credentialSubject": {
         "memberName": "Jane Smith",
         "membershipLevel": "Premium",
         "validUntil": "2026-03-19"
       }
     }
   }
   ```

### Example 2: Retrieving Schema Information

**User**: "What credential types do we currently support?"

**AI Processing**:
```json
{
  "name": "getAllSchemas",
  "parameters": {}
}
```

The AI would then process the results to provide a human-readable summary:
"We currently support the following credential types:
1. MembershipCredential (2 versions)
2. EventTicket 
3. ProofOfPurchase"