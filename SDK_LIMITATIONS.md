# MCP Kotlin SDK Limitations

This document tracks limitations in the Kotlin MCP SDK (version 0.8.1) that prevent full alignment with the [CANONICAL_INTERFACE.md](https://github.com/SamMorrowDrums/mcp-starters/blob/main/CANONICAL_INTERFACE.md).

## Resource Templates

### Issue
The Kotlin SDK does not expose an `addResourceTemplate()` method or equivalent API for registering URI template patterns.

### Impact
Resources that should appear in `resourceTemplates` (like `greeting://{name}` and `item://{id}`) instead appear as static resources in the `resources` list.

### Current Workaround
We use regular `addResource()` calls with base URIs (`greeting://` and `item://`) and extract parameters manually from incoming request URIs:

```kotlin
server.addResource(
    uri = "greeting://",
    name = "Personalized Greeting",
    description = "A personalized greeting for a specific person",
    mimeType = "text/plain"
) { request ->
    val uri = request.uri
    val name = uri.removePrefix("greeting://").takeIf { it.isNotEmpty() } ?: "friend"
    // ... handle request with extracted parameter
}
```

### Desired State
Ideally, the SDK would provide:
```kotlin
server.addResourceTemplate(
    uriTemplate = "greeting://{name}",
    name = "Personalized Greeting",
    description = "A personalized greeting for a specific person",
    mimeType = "text/plain"
) { request ->
    val name = request.parameters["name"] ?: "friend"
    // ... handle request with provided parameters
}
```

### Recommendation
- **Short-term**: Document this limitation and continue using the workaround
- **Long-term**: Open an issue or PR with the kotlin-sdk repository to add resource template support

### Status
🔴 **SDK Limitation** - Requires SDK changes

---

## Prompt Titles

### Issue
The `addPrompt()` method does not accept a `title` parameter. The method signature only supports:
- `name`: The prompt identifier
- `description`: The prompt description
- `arguments`: List of PromptArgument objects

### Impact
Prompts cannot include a user-friendly title field as specified in the canonical interface:
- `code_review` should have title `"Code Review"`
- `greet` should have title `"Greeting Prompt"`

### Current State
Only `name` and `description` are available:
```kotlin
server.addPrompt(
    name = "greet",
    description = "Generate a greeting message",
    arguments = listOf(...)
) { request -> ... }
```

### Desired State
```kotlin
server.addPrompt(
    name = "greet",
    title = "Greeting Prompt",  // Not currently supported
    description = "Generate a greeting message",
    arguments = listOf(...)
) { request -> ... }
```

### Note
PromptArgument objects (individual prompt arguments) DO support titles:
```kotlin
PromptArgument(
    name = "name",
    title = "Name",  // This IS supported
    description = "Name of the person to greet",
    required = true
)
```

### Recommendation
- **Short-term**: Use descriptive `name` values and detailed `description` fields
- **Long-term**: Request title field support in the kotlin-sdk

### Status
🔴 **SDK Limitation** - Requires SDK changes

---

## Summary

| Feature | Required | Supported | Status |
|---------|----------|-----------|--------|
| Resource Templates | ✅ | ❌ | SDK limitation |
| Prompt Titles | ✅ | ❌ | SDK limitation |
| Prompt Argument Titles | ✅ | ✅ | Implemented |
| Resource MIME Types | ✅ | ✅ | Implemented |

## Next Steps

1. ✅ Document limitations in SDK_LIMITATIONS.md
2. 🔲 Open issue in kotlin-sdk repository about resource template support
3. 🔲 Open issue in kotlin-sdk repository about prompt title support
4. 🔲 Monitor SDK updates for new features

## Resources

- [MCP Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk)
- [MCP Specification](https://modelcontextprotocol.io/)
- [Canonical Interface](https://github.com/SamMorrowDrums/mcp-starters/blob/main/CANONICAL_INTERFACE.md)
