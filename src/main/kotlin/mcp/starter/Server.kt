/**
 * MCP Kotlin Starter - Server
 *
 * Creates and configures the MCP server by combining
 * tools, resources, and prompts.
 *
 * @see https://modelcontextprotocol.io/
 */

package mcp.starter

import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.PromptArgument
import io.modelcontextprotocol.kotlin.sdk.types.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.Role
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import kotlinx.serialization.json.*

/**
 * Creates and configures the MCP server with all features.
 */
fun createServer(): Server {
    val server = Server(
        Implementation(
            name = "mcp-kotlin-starter",
            version = "1.0.0"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                prompts = ServerCapabilities.Prompts(listChanged = true)
            )
        )
    )

    registerTools(server)
    registerResources(server)
    registerPrompts(server)

    return server
}

/**
 * Register all tools with the server.
 */
private fun registerTools(server: Server) {
    // Hello tool
    server.addTool(
        name = "hello",
        description = "A friendly greeting tool that says hello to someone"
    ) { request ->
        val name = request.arguments?.get("name")?.jsonPrimitive?.content ?: "World"
        CallToolResult(content = listOf(TextContent("Hello, $name! Welcome to MCP.")))
    }

    // Weather tool
    server.addTool(
        name = "get_weather",
        description = "Get current weather for a location (simulated)"
    ) { request ->
        val location = request.arguments?.get("location")?.jsonPrimitive?.content ?: "Unknown"
        val conditions = listOf("sunny", "cloudy", "rainy", "windy")
        val weather = buildJsonObject {
            put("location", location)
            put("temperature", (15..35).random())
            put("unit", "celsius")
            put("conditions", conditions.random())
            put("humidity", (40..80).random())
        }
        CallToolResult(content = listOf(TextContent(Json.encodeToString(JsonObject.serializer(), weather))))
    }

    // Long task tool
    server.addTool(
        name = "long_task",
        description = "A task that takes time and reports progress along the way"
    ) { request ->
        val taskName = request.arguments?.get("taskName")?.jsonPrimitive?.content ?: "unnamed"
        val steps = 5
        
        // Simulate progress (in real implementation, send progress notifications)
        repeat(steps) {
            kotlinx.coroutines.delay(200) // 200ms per step
        }
        
        CallToolResult(content = listOf(TextContent("Task \"$taskName\" completed successfully after $steps steps!")))
    }

    // Calculate tool
    server.addTool(
        name = "calculate",
        description = "Perform basic arithmetic operations"
    ) { request ->
        val a = request.arguments?.get("a")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val b = request.arguments?.get("b")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val operation = request.arguments?.get("operation")?.jsonPrimitive?.content ?: "add"
        
        val result = when (operation) {
            "add" -> a + b
            "subtract" -> a - b
            "multiply" -> a * b
            "divide" -> if (b != 0.0) a / b else Double.NaN
            else -> Double.NaN
        }
        
        CallToolResult(content = listOf(TextContent("$a $operation $b = $result")))
    }

    // Echo tool
    server.addTool(
        name = "echo",
        description = "Echo back the provided message"
    ) { request ->
        val message = request.arguments?.get("message")?.jsonPrimitive?.content ?: ""
        CallToolResult(content = listOf(TextContent(message)))
    }

    // Reverse tool
    server.addTool(
        name = "reverse",
        description = "Reverse a string"
    ) { request ->
        val text = request.arguments?.get("text")?.jsonPrimitive?.content ?: ""
        CallToolResult(content = listOf(TextContent(text.reversed())))
    }
}

/**
 * Register all resources with the server.
 */
private fun registerResources(server: Server) {
    // About resource
    server.addResource(
        uri = "info://about",
        name = "About",
        description = "Information about this MCP server",
        mimeType = "text/plain"
    ) { request ->
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = """
                        MCP Kotlin Starter v1.0.0
                        
                        This is a feature-complete MCP server demonstrating:
                        - Tools with structured output
                        - Resources (static and dynamic)
                        - Prompts with completions
                        - Multiple transport options (stdio, HTTP)
                        
                        For more information, visit: https://modelcontextprotocol.io
                    """.trimIndent(),
                    uri = request.uri,
                    mimeType = "text/plain"
                )
            )
        )
    }

    // Example document resource
    server.addResource(
        uri = "doc://example",
        name = "Example Document",
        description = "An example markdown document",
        mimeType = "text/markdown"
    ) { request ->
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = """
                        # Example Document
                        
                        This is an example markdown document served as an MCP resource.
                        
                        ## Features
                        
                        - **Bold text** and *italic text*
                        - Lists and formatting
                        - Code blocks
                        
                        ```kotlin
                        val hello = "world"
                        ```
                        
                        ## Links
                        
                        - [MCP Documentation](https://modelcontextprotocol.io)
                        - [Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk)
                    """.trimIndent(),
                    uri = request.uri,
                    mimeType = "text/markdown"
                )
            )
        )
    }

    // Settings resource
    server.addResource(
        uri = "config://settings",
        name = "Server Settings",
        description = "Server configuration settings",
        mimeType = "application/json"
    ) { request ->
        val settings = buildJsonObject {
            put("version", "1.0.0")
            put("name", "mcp-kotlin-starter")
            putJsonObject("capabilities") {
                put("tools", true)
                put("resources", true)
                put("prompts", true)
            }
            putJsonObject("settings") {
                put("precision", 2)
                put("allow_negative", true)
            }
        }
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = Json.encodeToString(JsonObject.serializer(), settings),
                    uri = request.uri,
                    mimeType = "application/json"
                )
            )
        )
    }
}

/**
 * Register all prompts with the server.
 */
private fun registerPrompts(server: Server) {
    // Greet prompt
    server.addPrompt(
        name = "greet",
        description = "Generate a greeting in a specific style",
        arguments = listOf(
            PromptArgument(name = "name", description = "Name to greet", required = true),
            PromptArgument(name = "style", description = "Greeting style (formal, casual, enthusiastic)", required = false)
        )
    ) { request ->
        val name = request.arguments?.get("name") ?: "friend"
        val style = request.arguments?.get("style") ?: "casual"
        
        val text = when (style) {
            "formal" -> "Please compose a formal, professional greeting for $name."
            "casual" -> "Write a casual, friendly hello to $name."
            "enthusiastic" -> "Create an excited, enthusiastic greeting for $name!"
            else -> "Write a casual, friendly hello to $name."
        }
        
        GetPromptResult(
            description = "Greeting prompt for $name in $style style",
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(text)
                )
            )
        )
    }

    // Code review prompt
    server.addPrompt(
        name = "code_review",
        description = "Request a code review with specific focus areas",
        arguments = listOf(
            PromptArgument(name = "code", description = "Code to review", required = true),
            PromptArgument(name = "language", description = "Programming language", required = false),
            PromptArgument(name = "focus", description = "Focus area (security, performance, readability, all)", required = false)
        )
    ) { request ->
        val code = request.arguments?.get("code") ?: ""
        val language = request.arguments?.get("language") ?: "unknown"
        val focus = request.arguments?.get("focus") ?: "all"
        
        val instruction = when (focus) {
            "security" -> "Focus on security vulnerabilities and potential exploits."
            "performance" -> "Focus on performance optimizations and efficiency issues."
            "readability" -> "Focus on code clarity, naming, and maintainability."
            else -> "Provide a comprehensive review covering security, performance, and readability."
        }
        
        val text = """
            Please review the following $language code. $instruction
            
            ```$language
            $code
            ```
        """.trimIndent()
        
        GetPromptResult(
            description = "Code review prompt with $focus focus",
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(text)
                )
            )
        )
    }
}
