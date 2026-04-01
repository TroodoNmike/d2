package com.troodon.d2.util

/**
 * Creates [ProcessBuilder] instances with sensitive environment variables removed.
 *
 * The D2 CLI process inherits the IDE's full environment by default.
 * This factory strips known secret-bearing variables (API keys, tokens,
 * credentials) so a compromised or malicious D2 binary cannot exfiltrate them.
 */
object D2ProcessFactory {

    private val SENSITIVE_ENV_VARS = setOf(
        // AWS
        "AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY", "AWS_SESSION_TOKEN",
        // GitHub / GitLab
        "GITHUB_TOKEN", "GH_TOKEN", "GITLAB_TOKEN", "GL_TOKEN",
        // NPM / package registries
        "NPM_TOKEN", "NUGET_API_KEY",
        // Databases
        "DATABASE_URL", "DB_PASSWORD", "PGPASSWORD", "MYSQL_PWD",
        // AI / LLM services
        "OPENAI_API_KEY", "ANTHROPIC_API_KEY",
        // Messaging / collaboration
        "SLACK_TOKEN", "SLACK_WEBHOOK", "SLACK_BOT_TOKEN",
        // Cloud providers
        "GOOGLE_APPLICATION_CREDENTIALS", "AZURE_CLIENT_SECRET", "AZURE_CLIENT_ID",
        // General
        "SECRET_KEY", "API_KEY", "API_SECRET",
    )

    /**
     * Creates a [ProcessBuilder] for the given command with sensitive
     * environment variables removed.
     */
    fun create(command: List<String>): ProcessBuilder {
        val pb = ProcessBuilder(command)
        val env = pb.environment()
        SENSITIVE_ENV_VARS.forEach { env.remove(it) }
        return pb
    }
}
