plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "com.troodon.d2"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
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
            <h3>Version 1.0.3</h3>
            <ul>
                <li>Added live SVG preview support for rendering D2 diagrams with compositions.</li>
                <li>Added a preview mode toggle (PNG or SVG/HTML) to the preview toolbar.</li>
                <li>Export now matches the active preview mode (.png or .svg).</li>
                <li>Changing D2 settings now automatically re-renders the preview.</li>
                <li>Added <code>--animate-interval=1000</code> to support multi-step diagrams (layers/scenarios/steps).</li>
                <li>Added a configurable auto-refresh debounce delay in D2 settings.</li>
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
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
