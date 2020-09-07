package org.eclipse.keyple.gradle

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class KeypleVersioningTest {

    val versioning = KeypleVersioning()

    @Test
    fun getLastAlphaVersion_whenNoAlphaExists() {
        val lastVersion = versioning
                .getLastAlphaVersionFrom("0.8.0-SNAPSHOT")
        assertThat(lastVersion).isNull()
    }

    @Test
    fun getNextAlphaVersion_whenNoAlphaExists() {
        val nextVersion = versioning
                .getNextAlphaVersionFrom("0.8.0-SNAPSHOT")
        assertThat(nextVersion).isEqualTo("0.8.0-alpha-1")
    }

    @Test
    fun getLastAlphaVersion_whenAlphaExists() {
        val lastVersion = versioning
                .getLastAlphaVersionFrom("0.9.0-SNAPSHOT")
        assertThat(lastVersion).matches("0\\.9\\.0-alpha-\\d+")
    }

    @Test
    fun getNextAlphaVersion_whenAlphaExists() {
        val nextVersion = versioning
                .getNextAlphaVersionFrom("0.9.0-SNAPSHOT")
        assertThat(nextVersion).matches("0\\.9\\.0-alpha-\\d+")
    }
}