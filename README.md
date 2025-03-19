# MCP Server for SSI Issuer Service

## Overview

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

The MCP server exposes the following tools to AI models:

### Schema Management
- **createSchema**: Create a new credential schema
- **getAllSchemas**: Retrieve all schemas
- **getSchemaById**: Get a specific schema by ID
- **deleteSchema**: Delete a schema
- **schemaExistsByType**: Check if a schema exists by type
- **getLatestSchemaByType**: Get the latest version of a schema by type

### Credential Issuance
- **createTargetedOffering**: Create a credential offering for a specific recipient
- **createOpenOffering**: Create a credential offering available to anyone
- **issueCredential**: Issue a credential using an access token

## Example AI Interactions

When connected to an AI assistant, users can issue natural language requests that trigger tool calls:

**User**: "Create a ticket credential for the Spring Conference with seat A12."

**AI's Tool Call**:
```json
{
  "name": "createOpenOffering",
  "parameters": {
    "type": "EventTicket",
    "credentialSubject": {
      "ticketId": "T12345",
      "eventName": "Spring Conference 2025",
      "seat": "A12"
    }
  }
}
```

**User**: "Show me all available credential schemas."

**AI's Tool Call**:
```json
{
  "name": "getAllSchemas",
  "parameters": {}
}
```

## Future Enhancements

- **Improved AI Prompting**: Enhanced documentation to guide AI models in making appropriate tool calls
- **Conversational State Management**: Remember context across multiple tool calls in a conversation
- **Credential Revocation Tools**: Add capabilities for managing credential lifecycle
- **Multi-Issuer Support**: Allow a single MCP server to connect to multiple issuer services
- **Advanced Authentication Flows**: Support for more complex wallet interaction patterns
