plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "com.troodon.d2"
version = "1.0-"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdea("2024.3")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)


        // Add plugin dependencies for compilation here, example:
        // bundledPlugin("com.intellij.java")
    }
    testImplementation(kotlin("test"))
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }

        changeNotes = """
            <h3>Version 1.0.0</h3>
            <ul>
                <li>Initial release</li>
                <li>D2 language syntax highlighting</li>
                <li>Live preview with auto-refresh</li>
                <li>Split editor mode (code + preview)</li>
                <li>Zoom and pan controls for diagrams</li>
                <li>Export diagrams to PNG</li>
                <li>Auto-format with d2 fmt</li>
                <li>Configurable D2 CLI path</li>
                <li>Brace matching and code commenting</li>
                <li>Color settings customization</li>
            </ul>
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    test {
        jvmArgs("-Xshare:off")  // Disable CDS to avoid warning about custom class loader
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
