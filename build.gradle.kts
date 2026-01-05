plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "com.troodon.d2"
version = "1.0.7"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

fun getPluginDescription(): String {
    val readmeFile = file("README.md")
    if (!readmeFile.exists()) {
        return "D2 language support for IntelliJ-based IDEs"
    }

    val lines = readmeFile.readLines()
    val featuresSection = mutableListOf<String>()
    val requirementsSection = mutableListOf<String>()

    var inFeaturesSection = false
    var inRequirementsSection = false

    for (line in lines) {
        when {
            line.startsWith("## ✨ Features") || line.startsWith("## Features") -> {
                inFeaturesSection = true
                inRequirementsSection = false
                continue
            }
            line.startsWith("## 📋 Requirements") || line.startsWith("## Requirements") -> {
                inFeaturesSection = false
                inRequirementsSection = true
                continue
            }
            line.startsWith("##") -> {
                // Stop when we hit another section
                inFeaturesSection = false
                inRequirementsSection = false
            }
            inFeaturesSection && line.startsWith("- ") -> {
                // Remove emoji and markdown formatting
                var cleanLine = line.trimStart()
                    .removePrefix("- ")
                    .replace(Regex("^[🎨👁⚡🖼🖱📤🔧⏱⌨💡🎯⚙][️\\uFE0F]?\\s*"), "") // Remove emojis and variation selectors
                    .trim()

                // Convert markdown code blocks (`text`) to HTML <code>text</code>
                val parts = cleanLine.split("`")
                cleanLine = parts.mapIndexed { index, part ->
                    if (index % 2 == 1) "<code>$part</code>" else part
                }.joinToString("")

                // Extract feature name and convert remaining ** to <strong>
                val featureNameMatch = Regex("^\\*\\*([^*]+)\\*\\*").find(cleanLine)
                if (featureNameMatch != null) {
                    val featureName = featureNameMatch.groupValues[1]
                    var description = cleanLine.substring(featureNameMatch.range.last + 1)
                        .removePrefix(" - ")
                        .trim()
                    // Convert any remaining **text** to <strong>text</strong>
                    description = description.replace(Regex("\\*\\*([^*]+)\\*\\*"), "<strong>$1</strong>")
                    featuresSection.add("<li><strong>$featureName</strong> - $description</li>")
                } else {
                    featuresSection.add("<li>$cleanLine</li>")
                }
            }
            inRequirementsSection && line.isNotBlank() && !line.startsWith("###") && !line.startsWith("```") -> {
                requirementsSection.add(line)
            }
        }
    }

    val featuresHtml = if (featuresSection.isNotEmpty()) {
        """
        <h3>Features</h3>
        <ul>
            ${featuresSection.joinToString("\n            ")}
        </ul>
        """.trimIndent()
    } else ""

    val requirementsHtml = if (requirementsSection.isNotEmpty()) {
        var reqText = requirementsSection.joinToString(" ").trim()
        // Convert markdown bold (**text**) to HTML
        reqText = reqText.replace(Regex("\\*\\*([^*]+)\\*\\*"), "<strong>$1</strong>")
        """
        <h3>Requirements</h3>
        <p>$reqText Install from <a href="https://d2lang.com">d2lang.com</a></p>
        """.trimIndent()
    } else ""

    return """
<h2>D2 Language Support</h2>
<p>Comprehensive support for D2 (Declarative Diagramming) language in IntelliJ-based IDEs.</p>

$featuresHtml

$requirementsHtml
""".trimIndent()
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
            // Extract version number from ## [1.0.4] or ## [1.0.4] - 2026-01-02
            version = line.substringAfter("[").substringBefore("]")
            continue
        }
        if (foundFirstVersion && line.isNotBlank() && !line.startsWith("###")) {
            // Skip section headers like ### Added, ### Changed, ### Fixed
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

        description = getPluginDescription()
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
