import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()  // Or jvm("desktop") in some templates

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Add any shared deps here if you extract logic later
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {  // <-- Your JVM/Desktop-specific deps go here
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // Your original dependencies (updated to latest versions)
            implementation("org.apache.pdfbox:pdfbox:2.0.32") // Or add to libs.versions.toml
            implementation("org.apache.poi:poi-ooxml:5.3.0")
            implementation("io.ktor:ktor-client-cio:3.0.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
            implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.multai.sftpro.MainKt"  // Keep/adjust as needed

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "sftpro"  // <-- Change this to your desired base name (lowercase recommended)
            packageVersion = "1.0.0"

            // Optional: Add valid windows-specific settings here if needed (no exeName anymore)
            // windows {
            //     console = false
            //     shortcut = true
            //     menuGroup = "SFT-Pro"
            //     // etc.
            // }
        }
    }
}