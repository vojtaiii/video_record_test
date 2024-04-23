import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.github.sarxos:webcam-capture:0.3.12")
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.5.3")
    implementation("io.humble:humble-video-all:0.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "org.example.MainKt"
        nativeDistributions {
            //appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            includeAllModules = true
            vendor = "SAMI"
            targetFormats(TargetFormat.Exe)
            windows {
                packageVersion = "1.0.0"
            }
            //modules("java.instrument", "jdk.unsupported")
        }
    }
}

kotlin {
    jvmToolchain(19)
}