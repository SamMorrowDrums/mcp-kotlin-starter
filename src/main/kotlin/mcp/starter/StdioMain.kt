/**
 * MCP Kotlin Starter - stdio transport
 *
 * This entry point runs the MCP server using standard input/output,
 * suitable for CLI integration and local development.
 *
 * @see https://modelcontextprotocol.io/
 */

package mcp.starter

import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

/**
 * Main entry point for stdio transport.
 */
fun main() {
    val server = createServer()
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )

    runBlocking {
        server.createSession(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}
