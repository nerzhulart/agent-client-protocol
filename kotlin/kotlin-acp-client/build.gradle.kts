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
                implementation(project(":kotlin-acp-test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }
        }

        jvmTest {
            dependencies {
                implementation("ch.qos.logback:logback-classic:1.4.14")
            }
        }
    }
}