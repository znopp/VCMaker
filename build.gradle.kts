plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "pw.znopp"
version = "0.2-dev"
description = "A voice channel maker intended for private-adjacent use."

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.2.2") {
        exclude(module = "opus-java")
    }
    implementation("org.slf4j:slf4j-api:2.0.15") // SLF4J API
    implementation("ch.qos.logback:logback-classic:1.5.15") // Logback classic
    implementation("ch.qos.logback:logback-core:1.5.15") // Logback core
    implementation("com.google.code.gson:gson:2.11.0") // Gson
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "pw.znopp.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}