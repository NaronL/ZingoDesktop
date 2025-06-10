import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
}

group = "ru.igrakov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.uiTooling)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
}

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "ru.igrakov.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "Zingo"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("src/main/resources/logo.ico"))
            }
        }
    }
}
