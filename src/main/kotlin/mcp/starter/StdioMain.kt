/**
 * MCP Kotlin Starter - stdio Transport
 *
 * This entrypoint runs the MCP server using stdio transport,
 * which is ideal for local development and CLI tool integration.
 *
 * Usage:
 *   ./gradlew runStdio
 *   java -jar build/libs/mcp-kotlin-starter-all.jar
 *
 * @see https://modelcontextprotocol.io/docs/develop/transports#stdio
 */

package mcp.starter

import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val server = createServer()
    
    // Create stdio transport
    val transport = StdioServerTransport()
    
    // Connect the server to the transport
    server.connect(transport)
    
    // Log to stderr so it doesn't interfere with stdio protocol
    System.err.println("MCP Kotlin Starter running on stdio")
    System.err.println("Press Ctrl+C to exit")
    
    // Keep the server running
    transport.awaitClose()
}
