plugins {
    id("java")
}

group = "pw.znopp"
version = "0.1-dev"
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