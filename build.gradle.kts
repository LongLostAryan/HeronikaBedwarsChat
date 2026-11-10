plugins {
    id("java-library")
}

group = "org.heronika"
version = "1.0-SNAPSHOT"
description = "Heronika Bedwars chat"

repositories {
    mavenCentral()

    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

    // Legacy BungeeCord Chat 1.8 dependency mirror
    maven("https://repo.stellardrift.ca/maven/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("libs/BedWars1058-v23.2.1.jar"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.release.set(8)
        options.encoding = "UTF-8"
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "description" to project.description
        )

        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        archiveBaseName.set("HeronikaBedwarsChat")
    }
}