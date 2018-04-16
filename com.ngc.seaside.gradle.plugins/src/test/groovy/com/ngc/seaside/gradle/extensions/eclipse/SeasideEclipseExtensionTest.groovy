package com.ngc.seaside.gradle.extensions.eclipse

import com.ngc.seaside.gradle.extensions.eclipse.feature.SeasideEclipseExtension
import org.gradle.api.Project
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class SeasideEclipseExtensionTest {
    private static final String TEST_PROJECT_VERSION = "1.2.3-SNAPSHOT"
    private static final String TEST_PROJECT_GROUP = "test.project.group"
    private static final String TEST_PROJECT_NAME = "test-project-name"
    private static final String TEST_ARCHIVE_NAME =
          "${TEST_PROJECT_GROUP}.${TEST_PROJECT_NAME}-${TEST_PROJECT_VERSION}.zip"

    @Mock
    private Project project

    @Before
    void before() {
        when(project.getGroup()).thenReturn(TEST_PROJECT_GROUP)
        when(project.getName()).thenReturn(TEST_PROJECT_NAME)
        when(project.getVersion()).thenReturn(TEST_PROJECT_VERSION)
    }

    @Test
    void hasArchiveNameProperty() {
        def extension = new SeasideEclipseExtension(project)

        Assert.assertNotNull("archiveName property doesn't exist!", extension.archiveName)
        Assert.assertEquals("default archive name is incorrect!", extension.archiveName, TEST_ARCHIVE_NAME)
    }
}
