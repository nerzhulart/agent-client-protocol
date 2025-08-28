# ACP Kotlin SDK

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blueviolet?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![JVM](https://img.shields.io/badge/Platform-JVM-orange?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Kotlin implementation of the [Agent Client Protocol](https://agentclientprotocol.com) (ACP), providing both client and agent capabilities for integrating with AI agents across various platforms.

## Overview

The Agent Client Protocol allows applications to provide a standardized interface for AI agents, enabling seamless communication between clients (like code editors) and agents (AI assistants). This SDK implements the ACP specification for Kotlin, currently targeting JVM with future multiplatform support planned.

- Build ACP clients that can connect to any ACP agent
- Create ACP agents that expose capabilities to clients  
- Use standard transports like STDIO, WebSocket, and SSE
- Handle all ACP protocol messages and lifecycle events
- Full support for sessions, tool calls, permissions, and file system operations

## Project Structure

- **kotlin-acp-core**: Core types, protocol definitions, and shared components
- **kotlin-acp-agent**: Agent-side implementation for building ACP agents
- **kotlin-acp-client**: Client-side implementation for building ACP clients
- **kotlin-acp**: Meta-module that depends on all others for convenient importing
- **kotlin-acp-test**: Testing utilities and helpers
- **samples/**: Example implementations demonstrating usage

## Installation

Add the repository to your build file:

```kotlin
repositories {
    mavenCentral()
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("io.agentclientprotocol:kotlin-acp:0.1.0-SNAPSHOT")
}
```

For specific components:

```kotlin
dependencies {
    // For building agents
    implementation("io.agentclientprotocol:kotlin-acp-agent:0.1.0-SNAPSHOT")
    
    // For building clients  
    implementation("io.agentclientprotocol:kotlin-acp-client:0.1.0-SNAPSHOT")
    
    // For testing
    testImplementation("io.agentclientprotocol:kotlin-acp-test:0.1.0-SNAPSHOT")
}
```

## Quick Start

### Creating an Agent

```kotlin
import io.agentclientprotocol.kotlin.*
import io.agentclientprotocol.kotlin.agent.*
import io.agentclientprotocol.kotlin.transport.StdioTransport

class MyAgent : Agent {
    override suspend fun initialize(request: InitializeRequest): InitializeResponse {
        return InitializeResponse(
            protocolVersion = LATEST_PROTOCOL_VERSION,
            agentCapabilities = AgentCapabilities(
                promptCapabilities = PromptCapabilities(
                    image = true,
                    embeddedContext = true
                )
            )
        )
    }
    
    override suspend fun newSession(request: NewSessionRequest): NewSessionResponse {
        val sessionId = SessionId("session-${System.currentTimeMillis()}")
        return NewSessionResponse(sessionId)
    }
    
    override suspend fun prompt(request: PromptRequest): PromptResponse {
        // Process the user's prompt and send updates via client connection
        return PromptResponse(StopReason.END_TURN)
    }
    
    // Implement other required methods...
}

// Set up agent with STDIO transport
val agent = MyAgent()
val transport = StdioTransport(System.`in`.asSource(), System.out.asSink())
val connection = AgentSideConnection(agent)

connection.connect(transport)
```

### Creating a Client

```kotlin
import io.agentclientprotocol.kotlin.*
import io.agentclientprotocol.kotlin.client.*
import io.agentclientprotocol.kotlin.transport.StdioTransport

class MyClient : ClientInterface {
    override suspend fun readTextFile(request: ReadTextFileRequest): ReadTextFileResponse {
        val content = File(request.path).readText()
        return ReadTextFileResponse(content)
    }
    
    override suspend fun writeTextFile(request: WriteTextFileRequest) {
        File(request.path).writeText(request.content)
    }
    
    override suspend fun requestPermission(request: RequestPermissionRequest): RequestPermissionResponse {
        // Present options to user and return their choice
        val selectedOption = request.options.first()
        return RequestPermissionResponse(
            RequestPermissionOutcome.Selected(selectedOption.optionId)
        )
    }
    
    override suspend fun sessionUpdate(notification: SessionNotification) {
        // Handle real-time updates from agent
        println("Agent update: ${notification.update}")
    }
}

// Set up client
val client = MyClient()
val transport = StdioTransport(agentProcess.inputStream.asSource(), agentProcess.outputStream.asSink())
val connection = ClientSideConnection(client)

connection.connect(transport)

// Initialize agent
val initResponse = connection.initialize(InitializeRequest(
    protocolVersion = LATEST_PROTOCOL_VERSION,
    clientCapabilities = ClientCapabilities(
        fs = FileSystemCapability(
            readTextFile = true,
            writeTextFile = true
        )
    )
))

// Create session and send prompts
val sessionResponse = connection.newSession(NewSessionRequest(
    cwd = "/path/to/working/directory",
    mcpServers = emptyList()
))

val promptResponse = connection.prompt(PromptRequest(
    sessionId = sessionResponse.sessionId,
    prompt = listOf(ContentBlock.Text("Hello, agent!"))
))
```

## Samples

The `samples/` directory contains complete working examples:

- **kotlin-acp-agent-sample**: A simple agent that echoes messages and demonstrates tool calls
- **kotlin-acp-client-sample**: A basic client that connects to agents and handles file operations

Run the samples:

```bash
# Run the agent sample
./gradlew :samples:kotlin-acp-agent-sample:run

# Run the client sample  
./gradlew :samples:kotlin-acp-client-sample:run
```

## Features

### Protocol Support
- ✅ Full ACP v1 protocol implementation
- ✅ JSON-RPC message handling with request/response correlation
- ✅ Support for all ACP message types (requests, responses, notifications)

### Agent Features
- ✅ Agent initialization and capability negotiation
- ✅ Session management (create, load, cancel)
- ✅ Prompt processing with real-time updates
- ✅ Tool call reporting and progress updates
- ✅ Execution plan reporting
- ✅ File system operations (via client)
- ✅ Permission requests

### Client Features  
- ✅ Client initialization and capability advertising
- ✅ File system operations (read/write text files)
- ✅ Permission handling and user prompts
- ✅ Real-time session update processing
- ✅ Agent lifecycle management

### Transport Support
- ✅ STDIO transport for command-line usage
- 🚧 WebSocket transport (planned)
- 🚧 SSE transport (planned)

### Multiplatform Support
- ✅ JVM target
- 🚧 JavaScript/Node.js (planned)
- 🚧 Native targets (planned)
- 🚧 WebAssembly (planned)

## Architecture

The SDK follows a clean architecture with clear separation of concerns:

```
┌─────────────────┐    ┌─────────────────┐
│   Agent App     │    │   Client App    │
├─────────────────┤    ├─────────────────┤
│ AgentSideConn   │    │ ClientSideConn  │
├─────────────────┤    ├─────────────────┤
│    Protocol     │    │    Protocol     │
├─────────────────┤    ├─────────────────┤
│   Transport     │    │   Transport     │
│  (STDIO/WS/SSE) │◄──►│  (STDIO/WS/SSE) │
└─────────────────┘    └─────────────────┘
```

- **Transport Layer**: Handles raw message transmission (STDIO, WebSocket, SSE)  
- **Protocol Layer**: Manages JSON-RPC framing, request correlation, and error handling
- **Connection Layer**: Provides type-safe ACP method calls and handles serialization
- **Application Layer**: Your agent or client implementation

## Contributing

Contributions are welcome! Please see the [contribution guidelines](../CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License—see the [LICENSE](../LICENSE) file for details.

## Related Projects

- [ACP TypeScript SDK](../typescript/) - TypeScript/JavaScript implementation
- [ACP Rust SDK](../rust/) - Rust implementation  
- [Agent Client Protocol Specification](https://agentclientprotocol.com) - Protocol documentation