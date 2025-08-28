plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(project(":kotlin-acp"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.logging)
    implementation(libs.kotlinx.io.core)
}

application {
    mainClass.set("io.agentclientprotocol.kotlin.samples.client.ClientSampleKt")
}

kotlin {
    jvmToolchain(21)
}