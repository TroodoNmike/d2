plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "com.troodon.d2"
version = "1.0.4"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

fun getLatestChangelog(): String {
    val changelogFile = file("CHANGELOG.md")
    if (!changelogFile.exists()) {
        return "No changelog available"
    }

    val lines = changelogFile.readLines()
    val changes = mutableListOf<String>()
    var foundFirstVersion = false
    var version = ""

    for (line in lines) {
        if (line.startsWith("## [")) {
            if (foundFirstVersion) {
                // Stop at the second version header
                break
            }
            foundFirstVersion = true
            // Extract version number from ## [1.0.4]
            version = line.substringAfter("[").substringBefore("]")
            continue
        }
        if (foundFirstVersion && line.isNotBlank()) {
            // Convert markdown list item to HTML
            var htmlLine = line.trimStart().removePrefix("- ")

            // Convert markdown code blocks (`text`) to HTML <code>text</code>
            val parts = htmlLine.split("`")
            htmlLine = parts.mapIndexed { index, part ->
                if (index % 2 == 1) "<code>$part</code>" else part
            }.joinToString("")

            changes.add("<li>$htmlLine</li>")
        }
    }

    return """
        <h3>Version $version</h3>
        <ul>
            ${changes.joinToString("\n            ")}
        </ul>
        <p><a href="https://github.com/TroodoNmike/d2/blob/main/CHANGELOG.md">See full changelog</a></p>
    """.trimIndent()
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

        changeNotes = getLatestChangelog()
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
