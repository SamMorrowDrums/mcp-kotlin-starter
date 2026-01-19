plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

val mcpVersion = "0.8.1"
val ktorVersion = "3.0.3"

dependencies {
    // MCP SDK
    implementation("io.modelcontextprotocol:kotlin-sdk:$mcpVersion")
    
    // Ktor for HTTP transport
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-sse:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    
    // IO (for Source/Sink)
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.15")
    
    // Testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("mcp.starter.MainKt")
}

// Task to run stdio transport
tasks.register<JavaExec>("runStdio") {
    group = "application"
    description = "Run the MCP server with stdio transport"
    mainClass.set("mcp.starter.StdioMainKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

// Task to run HTTP transport
tasks.register<JavaExec>("runHttp") {
    group = "application"
    description = "Run the MCP server with HTTP transport"
    mainClass.set("mcp.starter.HttpMainKt")
    classpath = sourceSets["main"].runtimeClasspath
}

// Create a fat JAR for distribution
tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Creates a fat JAR with all dependencies"
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    from(sourceSets.main.get().output)
    
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    
    manifest {
        attributes["Main-Class"] = "mcp.starter.StdioMainKt"
    }
}
