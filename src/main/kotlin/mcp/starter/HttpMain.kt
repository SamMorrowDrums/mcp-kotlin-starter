/**
 * MCP Kotlin Starter - HTTP transport
 *
 * This entry point runs the MCP server using HTTP with SSE,
 * suitable for remote/web deployment.
 *
 * @see https://modelcontextprotocol.io/
 */

package mcp.starter

import io.ktor.http.HttpMethod
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.cors.routing.CORS
import io.modelcontextprotocol.kotlin.sdk.server.mcp

/**
 * Main entry point for HTTP transport.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 3000
    
    println("Starting MCP HTTP server on port $port...")
    println("Connect via SSE at http://localhost:$port/sse")
    
    embeddedServer(CIO, host = "0.0.0.0", port = port) {
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Delete)
            allowNonSimpleContentTypes = true
            anyHost()
        }
        mcp {
            createServer()
        }
    }.start(wait = true)
}
