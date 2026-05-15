version = "1.0.0"

plugins {
    id("java-library")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.5-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    processResources {
        val props = mapOf("version" to version)
        filesMatching(listOf("plugin.yml")) {
            expand(props)
        }
    }

    jar {
        archiveFileName.set("BonemealableSugarCane-${version}.jar")
    }
}
