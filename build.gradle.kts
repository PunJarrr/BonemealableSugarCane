import xyz.jpenilla.runpaper.task.RunServer
import xyz.jpenilla.runtask.service.DownloadsAPIService

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
    // Compile against the OLDEST version we support (1.17). Newer servers load plugins built on
    // older API; the reverse is not true. If something doesn't compile here, it doesn't exist on
    // 1.17 servers either. Spigot API (not Paper API) so it also runs on Spigot.
    compileOnly("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")

    // Test-only: never shipped in the jar. Config tests use YamlConfiguration, which works without a server.
    testImplementation("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    // Compiled with Java 21, but to Java 16 class files: Minecraft 1.17 servers run on Java 16.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    compileJava {
        options.release = 16
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        // Without this, an incremental build can keep the old version in plugin.yml after a bump.
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
        from("credits.txt")
        from("LICENSE")
    }

    jar {
        archiveFileName = "BonemealableSugarCane-${project.version}.jar"
    }
}

// ---------------------------------------------------------------------------
// Test servers: one per Minecraft version range we promise to support, plus Folia and Purpur.
// Each gets its own run directory (run/<id>) and the Java version that Minecraft version needs.
// Gradle downloads missing JDKs (foojay, settings.gradle.kts).
//
//   ./gradlew runServer             -> Paper 26.3 (newest, Java 25)
//   ./gradlew runServer1_17_1       -> Paper 1.17.1 (oldest supported)
//   ./gradlew runFolia26_3          -> Folia 26.3
//   ./gradlew runPurpur26_3         -> Purpur 26.3
//   ./gradlew -q testServers        -> lists them all (used by scripts/smoke-test.sh)
//
// The first start stops at the Minecraft EULA: read https://aka.ms/MinecraftEULA and set
// eula=true in run/<id>/eula.txt to agree. scripts/smoke-test.sh accepts it for its own
// throwaway servers in build/smoke/.
// -PserverDir=<dir> runs a server in another directory (the smoke test uses this).
// ---------------------------------------------------------------------------
data class TestServer(val id: String, val platform: String, val minecraftVersion: String, val javaVersion: Int) {
    val taskName: String
        get() = when {
            platform == "paper" && id == "26.3" -> "runServer"
            platform == "paper" -> "runServer" + minecraftVersion.replace('.', '_')
            else -> "run" + platform.replaceFirstChar(Char::uppercase) + minecraftVersion.replace('.', '_')
        }
}

val testServers = listOf(
    TestServer("26.3", "paper", "26.3", 25),
    TestServer("1.21.11", "paper", "1.21.11", 21),
    TestServer("1.20.6", "paper", "1.20.6", 21),
    TestServer("1.20.4", "paper", "1.20.4", 17),
    TestServer("1.19.4", "paper", "1.19.4", 17),
    TestServer("1.18.2", "paper", "1.18.2", 17),
    TestServer("1.17.1", "paper", "1.17.1", 17),
    TestServer("folia-26.3", "folia", "26.3", 25),
    TestServer("purpur-26.3", "purpur", "26.3", 25),
)

val pluginJar = tasks.jar.flatMap { it.archiveFile }
val serverDirOverride = providers.gradleProperty("serverDir")

testServers.forEach { server ->
    val serverJava = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(server.javaVersion)
    }

    val configureServer: RunServer.() -> Unit = {
        minecraftVersion(server.minecraftVersion)
        runDirectory = serverDirOverride.map { layout.projectDirectory.dir(it) }
            .orElse(layout.projectDirectory.dir("run/${server.id}"))
        javaLauncher = serverJava
        jvmArgs("-Xms1G", "-Xmx2G")
        when (server.platform) {
            "folia" -> downloadsApiService = DownloadsAPIService.folia(project)
            "purpur" -> {
                // Purpur isn't one of run-paper's download services; fetch its jar directly.
                val purpurJar = layout.buildDirectory.file("purpur/purpur-${server.minecraftVersion}.jar")
                runJar(purpurJar)
                doFirst {
                    val file = purpurJar.get().asFile
                    if (!file.exists()) {
                        file.parentFile.mkdirs()
                        uri("https://api.purpurmc.org/v2/purpur/${server.minecraftVersion}/latest/download")
                            .toURL().openStream().use { input -> file.outputStream().use(input::copyTo) }
                    }
                }
            }
        }
    }

    if (server.taskName == "runServer") {
        // The default task from run-paper already picks up our plugin jar.
        tasks.named<RunServer>("runServer") { configureServer() }
    } else {
        tasks.register<RunServer>(server.taskName) {
            group = "run paper"
            description = "Runs a ${server.platform} ${server.minecraftVersion} test server with the plugin."
            configureServer()
            pluginJars(pluginJar)
        }
    }
}

tasks.register("testServers") {
    group = "run paper"
    description = "Lists the test servers: id, task name, platform, Minecraft version, Java version."
    val lines = testServers.map { "${it.id} ${it.taskName} ${it.platform} ${it.minecraftVersion} ${it.javaVersion}" }
    doLast { lines.forEach(::println) }
}
