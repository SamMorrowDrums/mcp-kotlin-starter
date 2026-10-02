plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    id("com.ncorti.ktfmt.gradle") version "0.27.0"
    application
}

group = "com.example"

version = "1.0.0"

repositories { mavenCentral() }

val mcpVersion = "0.15.0"
val ktorVersion = "3.6.0"

dependencyLocking { lockAllConfigurations() }

dependencies {
    // MCP SDK
    implementation("io.modelcontextprotocol:kotlin-sdk:$mcpVersion")

    // Ktor for HTTP transport
    implementation(platform("io.ktor:ktor-bom:$ktorVersion"))
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-sse:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // IO (for Source/Sink)
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.6.5")

    // Testing
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(kotlin("test"))
    testImplementation("io.modelcontextprotocol:kotlin-sdk-testing:$mcpVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    dependsOn("fatJar")
    systemProperty(
        "serverJar",
        layout.buildDirectory.file("libs/${project.name}-${project.version}-all.jar").get().asFile,
    )
}

ktfmt { kotlinLangStyle() }

kotlin { jvmToolchain(17) }

application { mainClass.set("mcp.starter.MainKt") }

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

    manifest { attributes["Main-Class"] = "mcp.starter.StdioMainKt" }
}
