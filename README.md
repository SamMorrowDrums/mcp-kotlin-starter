# MCP Kotlin Starter

[![CI](https://github.com/SamMorrowDrums/mcp-kotlin-starter/actions/workflows/ci.yml/badge.svg)](https://github.com/SamMorrowDrums/mcp-kotlin-starter/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![MCP](https://img.shields.io/badge/MCP-Model%20Context%20Protocol-purple)](https://modelcontextprotocol.io/)

A feature-complete Model Context Protocol (MCP) server template in Kotlin. This starter demonstrates all major MCP features with clean, production-ready code.

## 📚 Documentation

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk)
- [Building MCP Servers](https://modelcontextprotocol.io/docs/develop/build-server)

## ✨ Features

| Category | Feature | Description |
|----------|---------|-------------|
| **Tools** | `hello` | Basic greeting tool |
| | `get_weather` | Tool with structured JSON output |
| | `long_task` | Task with progress reporting |
| | `calculate` | Arithmetic operations |
| | `echo` | Echo back messages |
| **Resources** | `info://about` | Server information |
| | `doc://example` | Example markdown document |
| | `config://settings` | Server configuration |
| **Prompts** | `greet` | Greeting in various styles |
| | `code_review` | Code review with focus areas |

## ⚠️ Known Limitations

The Kotlin MCP SDK (v0.8.1) has some limitations. See [SDK_LIMITATIONS.md](SDK_LIMITATIONS.md) for full details:

- **Resource Templates**: The SDK doesn't support `addResourceTemplate()`. Parameterized resources use manual URI parsing.
- **Prompt Titles**: The `addPrompt()` method doesn't accept a `title` field.

## 🚀 Quick Start

### Prerequisites

- [JDK 17+](https://adoptium.net/)
- [Gradle](https://gradle.org/) (or use the wrapper)

### Installation

```bash
# Clone the repository
git clone https://github.com/SamMorrowDrums/mcp-kotlin-starter.git
cd mcp-kotlin-starter

# Build
./gradlew build
```

### Running the Server

**stdio transport** (for local development):
```bash
./gradlew runStdio
```

**HTTP transport** (for remote/web deployment):
```bash
./gradlew runHttp
# Server runs on http://localhost:3000
```

## 🔧 VS Code Integration

This project includes VS Code configuration for seamless development:

1. Open the project in VS Code
2. The MCP configuration is in `.vscode/mcp.json`
3. Build with the Gradle tasks
4. Test the server using VS Code's MCP tools

## 📁 Project Structure

```
.
├── src/main/kotlin/mcp/starter/
│   ├── Server.kt         # Server setup with tools, resources, prompts
│   ├── StdioMain.kt      # stdio transport entrypoint
│   └── HttpMain.kt       # HTTP transport entrypoint
├── src/main/resources/
│   └── logback.xml       # Logging configuration
├── .vscode/
│   └── mcp.json          # MCP server configuration
├── build.gradle.kts      # Gradle build file
├── settings.gradle.kts
├── server.json           # MCP discovery configuration
└── README.md
```

## 🛠️ Development

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create a fat JAR
./gradlew fatJar

# Clean build
./gradlew clean build
```

## 🔍 MCP Inspector

The [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector) is an essential development tool for testing and debugging MCP servers.

### Running Inspector

```bash
# First, build the fat JAR
./gradlew fatJar

# Then run with inspector
npx @modelcontextprotocol/inspector java -jar build/libs/mcp-kotlin-starter-1.0.0-all.jar
```

### What Inspector Provides

- **Tools Tab**: List and invoke all registered tools with parameters
- **Resources Tab**: Browse and read resources
- **Prompts Tab**: View and test prompt templates
- **Logs Tab**: See JSON-RPC messages between client and server

## 📖 Feature Examples

### Adding a Tool

```kotlin
server.addTool(
    name = "hello",
    description = "A friendly greeting tool"
) { request ->
    val name = request.arguments["name"]?.jsonPrimitive?.content ?: "World"
    listOf(TextContent("Hello, $name! Welcome to MCP."))
}
```

### Adding a Resource

```kotlin
server.addResource(
    uri = "config://settings",
    name = "Server Settings",
    mimeType = "application/json"
) {
    ReadResourceResult(
        contents = listOf(
            TextResourceContents(
                uri = "config://settings",
                mimeType = "application/json",
                text = """{"precision": 2}"""
            )
        )
    )
}
```

### Adding a Prompt

```kotlin
server.addPrompt(
    name = "greet",
    description = "Generate a greeting in a specific style"
) { request ->
    val name = request.arguments?.get("name") ?: "friend"
    GetPromptResult(
        messages = listOf(
            PromptMessage(
                role = Role.user,
                content = TextContent("Write a greeting for $name.")
            )
        )
    )
}
```

## 🔐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | HTTP server port | `3000` |

## 🤝 Contributing

Contributions welcome! Please ensure your changes maintain feature parity with other language starters.

## 📄 License

MIT License - see [LICENSE](LICENSE) for details.
