/**
 * MCP Kotlin Starter - Server
 *
 * Creates and configures the MCP server by combining
 * tools, resources, and prompts with tool annotations.
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
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.*

// =============================================================================
// TOOL ANNOTATIONS - Every tool SHOULD have annotations for AI assistants
//
// WHY ANNOTATIONS MATTER:
// Annotations enable MCP client applications to understand the risk level of
// tool calls. Clients can use these hints to implement safety policies.
//
// ANNOTATION FIELDS:
// - readOnlyHint: Tool only reads data, doesn't modify state
// - destructiveHint: Tool can permanently delete or modify data
// - idempotentHint: Repeated calls with same args have same effect
// - openWorldHint: Tool accesses external systems (web, APIs, etc.)
// =============================================================================

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
    ) {
        "An MCP server demonstrating tools, resources, and prompts. Includes dynamic tool loading and resource templates."
    }

    registerTools(server)
    registerResources(server)
    registerPrompts(server)

    return server
}

// Track whether bonus tool has been loaded (for dynamic tool demo)
private var bonusToolLoaded = false

/**
 * Register all tools with the server.
 */
private fun registerTools(server: Server) {
    // Hello tool - with annotations and input schema
    server.addTool(
        name = "hello",
        description = "Say hello to a person",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("name", buildJsonObject {
                    put("type", "string")
                    put("description", "Name of the person to greet")
                })
            },
            required = listOf("name")
        ),
        toolAnnotations = ToolAnnotations(
            title = "Say Hello",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        )
    ) { request ->
        val name = request.arguments?.get("name")?.jsonPrimitive?.content ?: "World"
        CallToolResult(content = listOf(TextContent("Hello, $name! Welcome to MCP.")))
    }

    // ==========================================================================
    // DYNAMIC TOOL LOADING
    // This demonstrates adding tools at runtime. When load_bonus_tool is called,
    // it registers a new tool (bonus_calculator) and the server notifies clients
    // via tools/list_changed notification (enabled by listChanged = true capability).
    // ==========================================================================
    
    // Load bonus tool - demonstrates dynamic tool loading
    server.addTool(
        name = "load_bonus_tool",
        description = "Dynamically loads an additional bonus calculator tool",
        toolAnnotations = ToolAnnotations(
            title = "Load Bonus Tool",
            readOnlyHint = false, // Modifies server state
            destructiveHint = false,
            idempotentHint = true, // Safe to call multiple times
            openWorldHint = false
        )
    ) { _ ->
        if (bonusToolLoaded) {
            CallToolResult(content = listOf(TextContent("Bonus tool is already loaded! Use 'bonus_calculator' tool.")))
        } else {
            // Register the bonus tool dynamically
            server.addTool(
                name = "bonus_calculator",
                description = "A dynamically loaded tool that calculates percentage bonuses",
                toolAnnotations = ToolAnnotations(
                    title = "Bonus Calculator",
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
                )
            ) { bonusRequest ->
                val amount = bonusRequest.arguments?.get("amount")?.jsonPrimitive?.doubleOrNull ?: 0.0
                val percentage = bonusRequest.arguments?.get("percentage")?.jsonPrimitive?.doubleOrNull ?: 10.0
                val bonus = amount * (percentage / 100.0)
                val total = amount + bonus
                CallToolResult(content = listOf(TextContent(
                    "Amount: $amount, Bonus ($percentage%): $bonus, Total: $total"
                )))
            }
            bonusToolLoaded = true
            // Note: The server automatically sends tools/list_changed notification
            // because we enabled listChanged = true in ServerCapabilities.Tools
            CallToolResult(content = listOf(TextContent(
                "Bonus tool loaded successfully! The 'bonus_calculator' tool is now available. " +
                "Call it with 'amount' and 'percentage' arguments."
            )))
        }
    }

    // Weather tool
    server.addTool(
        name = "get_weather",
        description = "Get the current weather for a city",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("city", buildJsonObject {
                    put("type", "string")
                    put("description", "City name to get weather for")
                })
            },
            required = listOf("city")
        ),
        toolAnnotations = ToolAnnotations(
            title = "Get Weather",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = false, // Results vary
            openWorldHint = false
        )
    ) { request ->
        val city = request.arguments?.get("city")?.jsonPrimitive?.content ?: "Unknown"
        val conditions = listOf("sunny", "cloudy", "rainy", "windy")
        val weather = buildJsonObject {
            put("city", city)
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
        description = "Simulate a long-running task with progress updates",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("taskName", buildJsonObject {
                    put("type", "string")
                    put("description", "Name for this task")
                })
                put("steps", buildJsonObject {
                    put("type", "integer")
                    put("description", "Number of steps to simulate")
                    put("default", 5)
                })
            },
            required = listOf("taskName")
        ),
        toolAnnotations = ToolAnnotations(
            title = "Long Running Task",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false
        )
    ) { request ->
        val taskName = request.arguments?.get("taskName")?.jsonPrimitive?.content ?: "unnamed"
        val steps = request.arguments?.get("steps")?.jsonPrimitive?.intOrNull ?: 5
        
        // Simulate progress (in real implementation, send progress notifications)
        repeat(steps) {
            kotlinx.coroutines.delay(200) // 200ms per step
        }
        
        CallToolResult(content = listOf(TextContent("Task \"$taskName\" completed successfully after $steps steps!")))
    }

    // Ask LLM tool
    server.addTool(
        name = "ask_llm",
        description = "Ask the connected LLM a question using sampling",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("prompt", buildJsonObject {
                    put("type", "string")
                    put("description", "The question or prompt to send to the LLM")
                })
                put("maxTokens", buildJsonObject {
                    put("type", "integer")
                    put("description", "Maximum tokens in response")
                    put("default", 100)
                })
            },
            required = listOf("prompt")
        ),
        toolAnnotations = ToolAnnotations(
            title = "Ask LLM",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = false,
            openWorldHint = false
        )
    ) { request ->
        val prompt = request.arguments?.get("prompt")?.jsonPrimitive?.content ?: ""
        val maxTokens = request.arguments?.get("maxTokens")?.jsonPrimitive?.intOrNull ?: 100
        
        // In a real implementation, this would use the sampling API
        // For now, return a placeholder response
        CallToolResult(content = listOf(TextContent(
            "This tool would ask the LLM: \"$prompt\" (max tokens: $maxTokens). " +
            "In a real implementation, this would use the MCP sampling API to query the connected LLM."
        )))
    }

    // Confirm action tool
    server.addTool(
        name = "confirm_action",
        description = "Request user confirmation before proceeding",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("action", buildJsonObject {
                    put("type", "string")
                    put("description", "Description of the action to confirm")
                })
                put("destructive", buildJsonObject {
                    put("type", "boolean")
                    put("description", "Whether the action is destructive")
                    put("default", false)
                })
            },
            required = listOf("action")
        ),
        toolAnnotations = ToolAnnotations(
            title = "Confirm Action",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = false,
            openWorldHint = false
        )
    ) { request ->
        val action = request.arguments?.get("action")?.jsonPrimitive?.content ?: ""
        val destructive = request.arguments?.get("destructive")?.jsonPrimitive?.booleanOrNull ?: false
        
        // In a real implementation, this would use the MCP roots API to prompt user
        // For now, return a placeholder response
        val warningMsg = if (destructive) " ⚠️ This is a destructive action!" else ""
        CallToolResult(content = listOf(TextContent(
            "Would you like to proceed with: \"$action\"?$warningMsg " +
            "In a real implementation, this would use the MCP roots API to request user confirmation."
        )))
    }

    // Get feedback tool
    server.addTool(
        name = "get_feedback",
        description = "Request feedback from the user",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("question", buildJsonObject {
                    put("type", "string")
                    put("description", "The question to ask the user")
                })
            },
            required = listOf("question")
        ),
        toolAnnotations = ToolAnnotations(
            title = "Get Feedback",
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = false,
            openWorldHint = true
        )
    ) { request ->
        val question = request.arguments?.get("question")?.jsonPrimitive?.content ?: ""
        
        // In a real implementation, this would use the MCP roots API to prompt user
        // For now, return a placeholder response
        CallToolResult(content = listOf(TextContent(
            "Asking user: \"$question\". " +
            "In a real implementation, this would use the MCP roots API to request user feedback."
        )))
    }
}

/**
 * Register all resources with the server.
 */
private fun registerResources(server: Server) {
    // About resource
    server.addResource(
        uri = "about://server",
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
        description = "An example document resource",
        mimeType = "text/plain"
    ) { request ->
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = """
                        # Example Document
                        
                        This is an example document served as an MCP resource.
                        
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
                    mimeType = "text/plain"
                )
            )
        )
    }

    // Resource templates - using pattern-matching URIs
    // Note: The Kotlin SDK 0.8.1 does not expose an addResourceTemplate method.
    // Resource templates are implemented as regular resources with dynamic URI handling.
    // These resources can handle parameterized URIs by extracting parameters from the URI string.
    // For proper resource template support, the SDK would need to expose addResourceTemplate or
    // a similar method to register URI patterns like "greeting://{name}".
    
    // Personalized Greeting template (greeting://{name})
    server.addResource(
        uri = "greeting://",
        name = "Personalized Greeting",
        description = "A personalized greeting for a specific person",
        mimeType = "text/plain"
    ) { request ->
        // Extract the name parameter from the URI
        val uri = request.uri
        val name = uri.removePrefix("greeting://").takeIf { it.isNotEmpty() } ?: "friend"
        
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = "Hello, $name! This is a personalized greeting just for you.",
                    uri = request.uri,
                    mimeType = "text/plain"
                )
            )
        )
    }

    // Item Data template (item://{id})
    server.addResource(
        uri = "item://",
        name = "Item Data",
        description = "Data for a specific item by ID",
        mimeType = "application/json"
    ) { request ->
        // Extract the id parameter from the URI
        val uri = request.uri
        val id = uri.removePrefix("item://").takeIf { it.isNotEmpty() } ?: "0"
        
        val itemData = buildJsonObject {
            put("id", id)
            put("name", "Item $id")
            put("description", "This is a dynamically generated item with ID: $id")
            put("created", "2024-01-01T00:00:00Z")
        }
        
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = Json.encodeToString(JsonObject.serializer(), itemData),
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
        description = "Generate a greeting message",
        arguments = listOf(
            PromptArgument(
                name = "name",
                title = "Name",
                description = "Name of the person to greet",
                required = true
            ),
            PromptArgument(
                name = "style",
                title = "Style",
                description = "Greeting style (formal/casual)",
                required = false
            )
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
        description = "Review code for potential improvements",
        arguments = listOf(
            PromptArgument(
                name = "code",
                title = "Code",
                description = "The code to review",
                required = true
            )
        )
    ) { request ->
        val code = request.arguments?.get("code") ?: ""
        
        val text = """
            Please review the following code for potential improvements, bugs, and best practices:
            
            ```
            $code
            ```
            
            Provide feedback on:
            - Code quality and readability
            - Potential bugs or edge cases
            - Performance considerations
            - Best practices and conventions
        """.trimIndent()
        
        GetPromptResult(
            description = "Code review prompt",
            messages = listOf(
                PromptMessage(
                    role = Role.User,
                    content = TextContent(text)
                )
            )
        )
    }
}
