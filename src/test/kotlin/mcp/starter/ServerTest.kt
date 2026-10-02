package mcp.starter

import io.modelcontextprotocol.kotlin.sdk.ExperimentalMcpApi
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.testing.ChannelTransport
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptRequest
import io.modelcontextprotocol.kotlin.sdk.types.GetPromptRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ListResourceTemplatesRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMcpApi::class)
class ServerTest {
    @Test
    fun `packaged stdio server keeps logging off stdout`() {
        val process =
            ProcessBuilder(
                    Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-jar",
                    requireNotNull(System.getProperty("serverJar")),
                )
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
        val executor = Executors.newSingleThreadExecutor()
        try {
            val firstLine =
                executor.submit(Callable { process.inputStream.bufferedReader().readLine() })
            val request =
                """{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{},"clientInfo":{"name":"stdio-regression","version":"1"}}}"""
            process.outputStream.bufferedWriter().apply {
                write(request)
                newLine()
                flush()
            }
            val response =
                Json.parseToJsonElement(requireNotNull(firstLine.get(10, TimeUnit.SECONDS)))
                    .jsonObject
            assertEquals(1, response.getValue("id").jsonPrimitive.int)
            assertTrue("result" in response, response.toString())
        } finally {
            process.destroy()
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                assertTrue(process.waitFor(5, TimeUnit.SECONDS), "stdio server did not terminate")
            }
            executor.shutdownNow()
        }
    }

    @Test
    fun `tools remain callable after SDK upgrade`() = withClient { client ->
        assertEquals(
            setOf(
                "hello",
                "get_weather",
                "long_task",
                "ask_llm",
                "confirm_action",
                "get_feedback",
                "load_bonus_tool",
            ),
            client.listTools().tools.map { it.name }.toSet(),
        )
        val greeting = client.callTool("hello", mapOf("name" to "Kotlin"))
        assertEquals(
            "Hello, Kotlin! Welcome to MCP.",
            assertIs<TextContent>(greeting.content.single()).text,
        )
        val task = client.callTool("long_task", mapOf("taskName" to "upgrade", "steps" to 0))
        assertEquals(
            "Task \"upgrade\" completed successfully after 0 steps!",
            assertIs<TextContent>(task.content.single()).text,
        )
    }

    @Test
    fun `static and templated resources retain their interface`() = withClient { client ->
        assertEquals(
            setOf("about://server", "doc://example"),
            client.listResources().resources.map { it.uri }.toSet(),
        )
        assertEquals(
            setOf("greeting://{name}", "item://{id}"),
            client
                .listResourceTemplates(ListResourceTemplatesRequest())
                .resourceTemplates
                .map { it.uriTemplate }
                .toSet(),
        )
        val greeting =
            client.readResource(
                ReadResourceRequest(ReadResourceRequestParams(uri = "greeting://Kotlin"))
            )
        assertEquals(
            "Hello, Kotlin! This is a personalized greeting just for you.",
            assertIs<TextResourceContents>(greeting.contents.single()).text,
        )
        val about =
            client.readResource(
                ReadResourceRequest(ReadResourceRequestParams(uri = "about://server"))
            )
        assertTrue(
            assertIs<TextResourceContents>(about.contents.single())
                .text
                .contains("MCP Kotlin Starter v1.0.0")
        )
    }

    @Test
    fun `prompts retain titles and argument handling`() = withClient { client ->
        val prompts = client.listPrompts().prompts.associateBy { it.name }
        assertEquals(setOf("greet", "code_review"), prompts.keys)
        assertEquals("Greeting Prompt", prompts.getValue("greet").title)
        val greeting =
            client.getPrompt(
                GetPromptRequest(
                    GetPromptRequestParams(
                        name = "greet",
                        arguments = mapOf("name" to "Kotlin", "style" to "formal"),
                    )
                )
            )
        assertEquals(
            "Please compose a formal, professional greeting for Kotlin.",
            assertIs<TextContent>(greeting.messages.single().content).text,
        )
    }

    private fun withClient(block: suspend (Client) -> Unit) = runBlocking {
        withTimeout(15_000) {
            val (clientTransport, serverTransport) = ChannelTransport.createLinkedPair()
            val server = createServer()
            val client =
                Client(Implementation(name = "dependency-regression-tests", version = "1.0.0"))
            try {
                server.createSession(serverTransport)
                client.connect(clientTransport)
                block(client)
            } finally {
                client.close()
                server.close()
            }
        }
    }
}
