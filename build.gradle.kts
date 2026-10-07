import xyz.jpenilla.runpaper.task.RunServer

version = "1.1.0"

plugins {
    id("java-library")
    // run-paper 3.1.0 needs Gradle 9.7+ (see gradle/wrapper/gradle-wrapper.properties)
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}

dependencies {
    // Compile against the OLDEST version we support (1.20.5) so we can't use newer API by accident.
    compileOnly("org.spigotmc:spigot-api:1.20.5-R0.1-SNAPSHOT")

    // Test-only: never shipped in the jar. Config tests use YamlConfiguration, which works without a server.
    testImplementation("org.spigotmc:spigot-api:1.20.5-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        val props = mapOf("version" to version)
        // Without this, an incremental build can keep the old version in plugin.yml after a bump.
        inputs.properties(props)
        filesMatching(listOf("plugin.yml")) {
            expand(props)
        }
    }

    jar {
        archiveFileName.set("BonemealableSugarCane-${version}.jar")
    }
}

// ---------------------------------------------------------------------------
// Test servers, one per Minecraft version range we promise to support.
// Each gets its own run directory (run/<version>) and the Java version that
// Minecraft version needs. Gradle downloads missing JDKs (foojay, settings.gradle.kts).
//
//   ./gradlew runServer          -> Paper 26.2    (latest, Java 25)
//   ./gradlew runServer1_21_11   -> Paper 1.21.11 (newest 1.21.x, Java 21)
//   ./gradlew runServer1_20_6    -> Paper 1.20.6  (oldest supported, Java 21)
// ---------------------------------------------------------------------------
data class TestServer(val taskName: String, val minecraftVersion: String, val javaVersion: Int)

val testServers = listOf(
    TestServer("runServer", "26.2", 25),
    TestServer("runServer1_21_11", "1.21.11", 21),
    TestServer("runServer1_20_6", "1.20.6", 21),
)

val pluginJar = tasks.jar.flatMap { it.archiveFile }

testServers.forEach { server ->
    val serverDir = layout.projectDirectory.dir("run/${server.minecraftVersion}")
    val serverJava = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(server.javaVersion)
    }

    val configureServer: RunServer.() -> Unit = {
        minecraftVersion(server.minecraftVersion)
        runDirectory.set(serverDir)
        javaLauncher.set(serverJava)
        jvmArgs("-Xms2G", "-Xmx2G")
        // Accept the Minecraft EULA for local test servers only.
        systemProperty("com.mojang.eula.agree", "true")
    }

    if (server.taskName == "runServer") {
        // The default task from run-paper already picks up our plugin jar.
        tasks.named<RunServer>("runServer") { configureServer() }
    } else {
        tasks.register<RunServer>(server.taskName) {
            group = "run paper"
            description = "Runs a Paper ${server.minecraftVersion} test server with the plugin."
            configureServer()
            pluginJars(pluginJar)
        }
    }
}
