plugins {
    id("acp.multiplatform")
    id("acp.publishing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":kotlin-acp-core"))
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}