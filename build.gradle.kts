@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.22"
    id("fabric-loom") version "1.2.7"
    `maven-publish`
}

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val fabric_kotlin_version: String by project
val targetJavaVersion = JavaVersion.VERSION_17

repositories {
    maven(url = "https://ueaj.dev/maven") {
        name = "ARRP"
        mavenContent {
            includeGroup("net.devtech")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")

    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabric_kotlin_version")

    modImplementation("net.devtech:arrp:0.6.7")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", minecraft_version)
        inputs.property("loader_version", loader_version)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "minecraft_version" to minecraft_version,
                "loader_version" to loader_version,
                "fabric_kotlin_version" to fabric_kotlin_version,
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        if (targetJavaVersion > JavaVersion.VERSION_1_10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion.majorVersion.toInt())
        }
    }

    java {
        if (JavaVersion.current() < targetJavaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.majorVersion))
        }
        withSourcesJar()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.version}" }
        }
    }

    kotlin {
        jvmToolchain(targetJavaVersion.majorVersion.toInt())
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = targetJavaVersion.majorVersion
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.archivesName.get()
                version = project.version.toString()
                from(project.components["java"])
            }
        }

        repositories {

        }
    }
}
