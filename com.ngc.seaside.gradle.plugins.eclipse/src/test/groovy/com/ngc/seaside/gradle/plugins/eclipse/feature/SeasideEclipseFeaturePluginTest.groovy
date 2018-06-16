package com.ngc.seaside.gradle.plugins.eclipse.feature

import com.ngc.seaside.gradle.util.TaskResolver

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SeasideEclipseFeaturePluginTest {
    private SeasideEclipseFeaturePlugin plugin
    private TaskResolver resolver
    private Project project
    private File projectDir
    private List<String> taskNames = [
          SeasideEclipseFeaturePlugin.ECLIPSE_CREATE_JAR_TASK_NAME,
          SeasideEclipseFeaturePlugin.ECLIPSE_COPY_FEATURE_FILE_TASK_NAME,
    ]

    @Before
    void before() {
        project = ProjectBuilder.builder().withName('test').build()
        project.setProperty('version', '1.2.3')
        project.setProperty('group', 'test')
        project.apply plugin: 'com.ngc.seaside.eclipse.feature'
        resolver = new TaskResolver(project)
    }

    @Test
    void tasksExist() {
        verifyTasksExistOnThePlugin()
    }

    @Test
    void extensionExists() {
        Assert.assertNotNull(
              "eclipse extension does not exist!",
              project.extensions.findByName(SeasideEclipseFeaturePlugin.ECLIPSE_FEATURE_EXTENSION_NAME)
        )
    }

    private void verifyTasksExistOnThePlugin() {
        taskNames.each { taskName ->
            Assert.assertNotNull(
                  "$taskName task does not exist!",
                  resolver.findTask(taskName)
            )
        }
    }
}