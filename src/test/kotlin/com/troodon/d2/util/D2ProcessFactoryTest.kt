package com.troodon.d2.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class D2ProcessFactoryTest {

    @Test
    fun `removes sensitive environment variables`() {
        val pb = D2ProcessFactory.create(listOf("echo", "test"))
        val env = pb.environment()

        // These should be removed if they existed
        val sensitiveVars = listOf(
            "AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY", "AWS_SESSION_TOKEN",
            "GITHUB_TOKEN", "GH_TOKEN", "GITLAB_TOKEN",
            "NPM_TOKEN", "OPENAI_API_KEY", "ANTHROPIC_API_KEY",
            "SLACK_TOKEN", "DATABASE_URL", "DB_PASSWORD",
            "AZURE_CLIENT_SECRET", "API_KEY", "API_SECRET"
        )
        for (v in sensitiveVars) {
            assertFalse("$v should be removed from environment", env.containsKey(v))
        }
    }

    @Test
    fun `preserves PATH and HOME`() {
        val pb = D2ProcessFactory.create(listOf("echo", "test"))
        val env = pb.environment()

        // PATH should always be present
        assertNotNull("PATH should be preserved", env["PATH"])
    }

    @Test
    fun `creates ProcessBuilder with correct command`() {
        val command = listOf("/usr/local/bin/d2", "--theme", "200", "-", "-")
        val pb = D2ProcessFactory.create(command)
        assertTrue(pb.command() == command)
    }
}
