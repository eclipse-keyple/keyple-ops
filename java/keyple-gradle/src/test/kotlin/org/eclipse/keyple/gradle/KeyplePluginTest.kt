package org.eclipse.keyple.gradle

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskOutputs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File
import java.nio.charset.Charset
import java.util.*


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
            doAnswer { mock(TaskOutputs::class.java) }
                .`when`(task).outputs
            task
        }.`when`(project).task(anyString())
        doReturn("1.0.0").`when`(project).version

        val properties = HashMap<String, Any>()
        doReturn(properties).`when`(project).properties
        val repositories = mock(RepositoryHandler::class.java)
        doReturn(repositories).`when`(project).repositories
        doReturn("org.eclipse.keyple").`when`(project).group

        val logger = mock(Logger::class.java)
        doReturn(logger).`when`(project).logger
        doAnswer { println(it.arguments[0]) }.`when`(logger).info(any())

        val plugins = mock(PluginContainer::class.java)
        doReturn(plugins).`when`(project).plugins

        val extensions = mock(ExtensionContainer::class.java)
        doReturn(extensions).`when`(project).extensions

        val tasks = mock(TaskContainer::class.java)
        doReturn(tasks).`when`(project).tasks

        val task = mock(Task::class.java)
        doReturn(task).`when`(tasks).getByName(anyString())

        doReturn(File("build")).`when`(project).rootDir
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
    fun setVersion_withEmptyProperties() {
        val plugin = KeyplePlugin()
        plugin.apply(project)
        val task = tasks["setVersion"]!!
        val file = File.createTempFile("test", "gradle.properties")
        val backup = File.createTempFile("test", "gradle.properties.bak")
        file.deleteOnExit()
        backup.deleteOnExit()
        doReturn(file)
            .`when`(project).file("gradle.properties")
        doReturn(backup)
            .`when`(project).file("gradle.properties.bak")
        doReturn("1.2.3")
            .`when`(project).version

        plugin.setVersion(task)

        assertThat(file.readText(Charset.forName("UTF-8"))).startsWith("version = 1.2.3")
    }

    @Test
    fun setVersion_withExistingProperties() {
        val plugin = KeyplePlugin()
        plugin.apply(project)
        val task = tasks["setVersion"]!!
        val file = File.createTempFile("test", "gradle.properties")
        val backup = File.createTempFile("test", "gradle.properties.bak")
        file.deleteOnExit()
        backup.deleteOnExit()
        file.printWriter(Charset.forName("UTF-8"))
            .use {
                it.println("first = 1")
                it.println("version = 1.0.0")
                it.println("second = 2")
            }
        doReturn(file)
            .`when`(project).file("gradle.properties")
        doReturn(backup)
            .`when`(project).file("gradle.properties.bak")
        doReturn("2.0.0")
            .`when`(project).version

        plugin.setVersion(task)

        assertThat(file.readLines(Charset.forName("UTF-8")))
            .containsExactly(
                "first = 1",
                "version = 2.0.0",
                "second = 2"
            )
    }

    @Test
    fun getLastAlphaVersion() {
        val plugin = KeyplePlugin()
        plugin.apply(project)
        val task = tasks["getLastAlphaVersion"]!!
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

        verify(project).version = "0.9.0-alpha-3"
    }
}