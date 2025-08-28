rootProject.name = "kotlin-acp-sdk"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":kotlin-acp-core")
include(":kotlin-acp-agent")
include(":kotlin-acp-client")
include(":kotlin-acp")
include(":kotlin-acp-test")

// Include sample projects
include(":samples:kotlin-acp-agent-sample")
include(":samples:kotlin-acp-client-sample")