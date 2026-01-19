/**
 * MCP Kotlin Starter - HTTP Transport
 *
 * This entrypoint runs the MCP server using HTTP with SSE streams,
 * which is ideal for remote deployment and web-based clients.
 *
 * Usage:
 *   ./gradlew runHttp
 *   PORT=8080 ./gradlew runHttp
 *
 * @see https://modelcontextprotocol.io/docs/develop/transports#streamable-http
 */

package mcp.starter

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import kotlinx.serialization.json.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 3000
    
    println("MCP Kotlin Starter running on http://localhost:$port")
    println("  MCP endpoint: http://localhost:$port/mcp")
    println("  Health check: http://localhost:$port/health")
    println()
    println("Press Ctrl+C to exit")
    
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(SSE)
    
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(
                mapOf(
                    "status" to "ok",
                    "server" to "mcp-kotlin-starter",
                    "version" to "1.0.0"
                )
            )
        }
        
        // MCP endpoint using SSE transport
        route("/mcp") {
            mcp {
                createServer()
            }
        }
    }
}
