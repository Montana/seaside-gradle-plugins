package com.ngc.seaside.gradle.util.eclipse

import com.ngc.seaside.gradle.extensions.eclipse.updatesite.SeasideEclipseUpdateSiteExtension
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assume.assumeFalse
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class EclipsePropertyUtilTest {
    private static final String TEST_PROJECT_VERSION = "1.2.3-SNAPSHOT"
    private static final String TEST_PROJECT_GROUP = "test.project.group"
    private static final String TEST_PROJECT_NAME = "test-project-name"
    private static final String TEST_GRADLE_USER_HOME = "/home/user/.gradle"
    private static final String TEST_LINUX_ECLIPSE_VERSION = "eclipse-dsl-oxygen-2-linux-gtk-x86_64"
    private static final String TEST_WINDOWS_ECLIPSE_VERSION = "eclipse-dsl-oxygen-2-win32-x86_64"
    private static final String TEST_LINUX_ECLIPSE_DOWNLOAD_URL = "http://1.2.3.4/${TEST_LINUX_ECLIPSE_VERSION}.zip"
    private static final String TEST_WINDOWS_ECLIPSE_DOWNLOAD_URL = "http://1.2.3.4/${TEST_WINDOWS_ECLIPSE_VERSION}.zip"

    private SeasideEclipseUpdateSiteExtension extension

    @Before
    void before() {
        def project = mock(Project.class)
        when(project.group).thenReturn(TEST_PROJECT_GROUP)
        when(project.name).thenReturn(TEST_PROJECT_NAME)
        when(project.version).thenReturn(TEST_PROJECT_VERSION)

        File file = mock(File.class)
        when(file.absolutePath).thenReturn(TEST_GRADLE_USER_HOME)

        Gradle gradle = mock(Gradle.class)
        when(gradle.gradleUserHomeDir).thenReturn(file)
        when(project.gradle).thenReturn(gradle)

        extension = new SeasideEclipseUpdateSiteExtension(project)
        extension.linuxEclipseVersion = TEST_LINUX_ECLIPSE_VERSION
        extension.windowsEclipseVersion = TEST_WINDOWS_ECLIPSE_VERSION
        extension.linuxDownloadUrl = TEST_LINUX_ECLIPSE_DOWNLOAD_URL
        extension.windowsDownloadUrl = TEST_WINDOWS_ECLIPSE_DOWNLOAD_URL
    }

    @Test
    void returnsCorrectEclipseVersionOnLinux() {
        assumeFalse(
              "Current OS is Windows, skipping Linux test.",
              System.getProperty("os.name").toLowerCase().startsWith("win")
        )
        assertEquals(
              "incorrect eclipse version returned on linux",
              TEST_LINUX_ECLIPSE_VERSION,
              EclipsePropertyUtil.getEclipseVersion(extension)
        )
    }

    @Test
    void returnsCorrectEclipseVersionOnWindows() {
        assumeFalse(
              "Current OS is Linux, skipping Windows test.",
              System.getProperty("os.name").toLowerCase().startsWith("linux")
        )
        assertEquals(
              "incorrect eclipse version returned on windows",
              TEST_WINDOWS_ECLIPSE_VERSION,
              EclipsePropertyUtil.getEclipseVersion(extension)
        )
    }
}
