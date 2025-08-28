plugins {
    id("acp.multiplatform")
    id("acp.publishing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":kotlin-acp-core"))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}