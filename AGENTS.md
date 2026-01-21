# AGENTS.md

This file provides context for AI coding agents working in this repository.

## Quick Reference

| Task | Command |
|------|---------|
| Build | `./gradlew build` |
| Build fat JAR | `./gradlew fatJar` |
| Test | `./gradlew test` |
| Format | `./gradlew ktfmtFormat` |
| Format check | `./gradlew ktfmtCheck` |
| Run (stdio) | `java -jar build/libs/mcp-kotlin-starter-1.0.0-all.jar` |
| Run (HTTP) | `./gradlew runHttp` |

## Project Overview

**MCP Kotlin Starter** is a Model Context Protocol (MCP) server template in Kotlin using the official kotlin-sdk. It demonstrates core MCP features including tools, resources, and prompts with both stdio and HTTP transports.

**Purpose**: Workshop starter template for learning MCP server development in Kotlin.

## Technology Stack

- **Runtime**: Kotlin 2.2.0, JDK 17
- **MCP SDK**: `io.modelcontextprotocol:kotlin-sdk:0.8.1`
- **HTTP Server**: Ktor 3.0.3 with CIO engine
- **Build Tool**: Gradle 8.11
- **Serialization**: kotlinx-serialization

## Project Structure

```
.
├── build.gradle.kts            # Build configuration
├── settings.gradle.kts         # Project settings
├── gradlew / gradlew.bat       # Gradle wrapper
├── server.json                 # MCP server configuration
├── SDK_LIMITATIONS.md          # Known SDK limitations and workarounds
├── src/
│   └── main/
│       └── kotlin/
│           └── mcp/
│               └── starter/
│                   ├── Server.kt       # Main server (tools, resources, prompts)
│                   ├── StdioMain.kt    # stdio transport entrypoint
│                   └── HttpMain.kt     # HTTP transport entrypoint
├── .vscode/
│   ├── mcp.json                # MCP server configuration
│   └── tasks.json              # Build/run tasks
└── .github/
    └── workflows/
        ├── ci.yml              # CI workflow
        └── conformance.yml     # MCP conformance tests
```

## Build & Run Commands

```bash
# Build project
./gradlew build

# Build fat JAR (includes all dependencies)
./gradlew fatJar

# Run server (stdio transport)
java -jar build/libs/mcp-kotlin-starter-1.0.0-all.jar

# Run server (HTTP transport)
./gradlew runHttp
# Server starts on http://localhost:3000/mcp
```

## Linting & Formatting

```bash
# Format code with ktfmt
./gradlew ktfmtFormat

# Check formatting (CI uses this)
./gradlew ktfmtCheck
```

## Testing

```bash
./gradlew test
```

## Key Files to Modify

- **Add/modify tools**: `src/main/kotlin/mcp/starter/Server.kt` → `registerTools()` function
- **Add/modify resources**: `src/main/kotlin/mcp/starter/Server.kt` → `registerResources()` function
- **Add/modify prompts**: `src/main/kotlin/mcp/starter/Server.kt` → `registerPrompts()` function
- **Server configuration**: `src/main/kotlin/mcp/starter/Server.kt` → `createServer()` function
- **HTTP config**: `src/main/kotlin/mcp/starter/HttpMain.kt`
- **Dependencies**: `build.gradle.kts`

## MCP Features Implemented

| Feature | Location | Description |
|---------|----------|-------------|
| `hello` tool | `Server.kt` | Basic greeting tool |
| `get_weather` tool | `Server.kt` | Structured JSON output |
| `long_task` tool | `Server.kt` | Simulated long-running task |
| `calculate` tool | `Server.kt` | Arithmetic operations |
| `echo` tool | `Server.kt` | Echo messages back |
| Resources | `Server.kt` | `info://about`, `doc://example`, `config://settings` |
| Prompts | `Server.kt` | `greet`, `code_review` with arguments |

## Environment Variables

- `PORT` - HTTP server port (default: 3000)

## Conventions

- Use `server.addTool()` to register tools
- Access tool arguments via `request.arguments?.get("param")?.jsonPrimitive?.content`
- Return `CallToolResult(content = listOf(TextContent(...)))` from tools
- Use `runBlocking` for coroutine entry points
- Follow Kotlin naming conventions (camelCase for functions/properties)

## Tool Pattern

```kotlin
server.addTool(
    name = "my_tool",
    description = "Tool description"
) { request ->
    val param = request.arguments?.get("param")?.jsonPrimitive?.content ?: "default"
    CallToolResult(content = listOf(TextContent("Result: $param")))
}
```

## Resource Pattern

```kotlin
server.addResource(
    uri = "my://resource",
    name = "My Resource",
    description = "Resource description",
    mimeType = "text/plain"
) { request ->
    ReadResourceResult(
        contents = listOf(
            TextResourceContents(
                text = "Resource content",
                uri = request.uri,
                mimeType = "text/plain"
            )
        )
    )
}
```

## Prompt Pattern

```kotlin
server.addPrompt(
    name = "my_prompt",
    description = "Prompt description",
    arguments = listOf(
        PromptArgument(name = "param", description = "Parameter", required = true)
    )
) { request ->
    val param = request.arguments?.get("param") ?: "default"
    GetPromptResult(
        description = "Generated prompt",
        messages = listOf(
            PromptMessage(
                role = Role.User,
                content = TextContent("Prompt text with $param")
            )
        )
    )
}
```

## Known SDK Limitations

The Kotlin MCP SDK (v0.8.1) has some limitations compared to other MCP implementations. See [SDK_LIMITATIONS.md](SDK_LIMITATIONS.md) for details:

1. **No Resource Templates Support**: The SDK doesn't expose `addResourceTemplate()`. Resources with URI parameters (like `greeting://{name}`) must be implemented using regular `addResource()` with manual URI parsing.

2. **No Prompt Title Field**: The `addPrompt()` method doesn't support a `title` parameter. Only `name`, `description`, and `arguments` are available.

These limitations are documented in the code with workarounds where possible.

## Documentation Links

- [MCP Specification](https://modelcontextprotocol.io/)
- [Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk)
- [Building Servers](https://modelcontextprotocol.io/docs/develop/build-server)
- [Ktor Documentation](https://ktor.io/docs/)
- [SDK Limitations](SDK_LIMITATIONS.md)
