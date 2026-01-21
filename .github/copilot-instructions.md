# MCP Kotlin Starter - Copilot Coding Agent Instructions

## Building and Testing

- **Build the project:**
  ```bash
  ./gradlew build
  ```

- **Run the server:**
  ```bash
  ./gradlew run
  ```

- **Run tests:**
  ```bash
  ./gradlew test
  ```

- **Clean build:**
  ```bash
  ./gradlew clean build
  ```

- **Check code (lint):**
  ```bash
  ./gradlew check
  ```

## Code Conventions

- Follow Kotlin coding conventions
- Use `data class` for DTOs
- Prefer immutable collections (`listOf`, `mapOf`)
- Use coroutines for async operations
- Use sealed classes for representing restricted hierarchies

## Before Committing Checklist

1. ✅ Run `./gradlew check` and fix any errors
2. ✅ Run `./gradlew build` to verify compilation
3. ✅ Run `./gradlew test` to verify tests pass
4. ✅ Test the server with `./gradlew run`

