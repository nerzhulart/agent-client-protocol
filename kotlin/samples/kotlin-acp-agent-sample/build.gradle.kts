plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(project(":kotlin-acp"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.logging)
    
    // For STDIO support on JVM
    implementation(libs.kotlinx.io.core)
}

application {
    mainClass.set("io.agentclientprotocol.kotlin.samples.agent.AgentSampleKt")
}

kotlin {
    jvmToolchain(21)
}