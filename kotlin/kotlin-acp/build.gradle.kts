plugins {
    id("acp.multiplatform")
    id("acp.publishing")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":kotlin-acp-core"))
                api(project(":kotlin-acp-agent"))
                api(project(":kotlin-acp-client"))
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}