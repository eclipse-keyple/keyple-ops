package org.eclipse.keyple.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File
import java.nio.charset.Charset


internal class KeyplePluginTest {

    private lateinit var project: Project
    private val tasks = HashMap<String, Task>()

    @BeforeEach
    internal fun setUp() {
        project = mock(Project::class.java)
        doAnswer {
            val name = it.getArgument<String>(0)
            val task = mock(Task::class.java)
            tasks.put(name, task)
            doAnswer { project }
                    .`when`(task).project
            doAnswer { task }
                    .`when`(task).doFirst(any<Action<Task>>())
            task
        }.`when`(project).task(anyString())
        doReturn("1.0.0").`when`(project).version
        doReturn("org.eclipse.keyple").`when`(project).group

        val plugins = mock(PluginContainer::class.java)
        doReturn(plugins).`when`(project).plugins

        val extensions = mock(ExtensionContainer::class.java)
        doReturn(extensions).`when`(project).extensions
    }

    @Test
    fun tasksAreCorrectlyInserted() {
        val plugin = KeyplePlugin()

        plugin.apply(project)

        verify(project)
                .task("setVersion")

        verify(tasks["setVersion"]!!)
                .doFirst(any<Action<Task>>())

        verify(project)
                .task("getLastAlphaVersion")

        verify(tasks["getLastAlphaVersion"]!!)
                .doFirst(any<Action<Task>>())

        verify(project)
                .task("setNextAlphaVersion")

        verify(tasks["setNextAlphaVersion"]!!)
                .doFirst(any<Action<Task>>())
    }

    @Test
    fun setVersion() {
        val plugin = KeyplePlugin()
        plugin.apply(project)
        val task = tasks["setVersion"]!!
        val file = File.createTempFile("test", "gradle.properties")
        file.deleteOnExit()
        doReturn(file)
                .`when`(project).file("gradle.properties")
        doReturn("1.2.3")
                .`when`(project).version

        plugin.setVersion(task)

        assertThat(file.readText(Charset.forName("UTF-8"))).startsWith("version=1.2.3")
    }

    @Test
    fun getLastAlphaVersion() {
        val plugin = KeyplePlugin()
        plugin.apply(project)
        val task = tasks["getLastAlphaVersion"] !!
        doReturn("0.9.0")
                .`when`(project).version

        plugin.getLastAlphaVersion(task)
    }

    @Test
    fun setNextAlphaVersion() {
        val plugin = KeyplePlugin()
        plugin.apply(project)
        doReturn("0.9.0")
                .`when`(project).version

        plugin.setNextAlphaVersion(tasks["setNextAlphaVersion"]!!)

        verify(project)
                .setVersion("0.9.0-alpha-3")
    }
}