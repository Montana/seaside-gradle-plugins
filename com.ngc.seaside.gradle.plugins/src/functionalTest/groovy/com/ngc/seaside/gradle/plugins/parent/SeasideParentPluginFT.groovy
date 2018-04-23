package com.ngc.seaside.gradle.plugins.parent

import com.ngc.seaside.gradle.util.test.SeasideGradleRunner
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Before
import org.junit.Test

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assume.assumeNoException
import static org.junit.Assume.assumeTrue

class SeasideParentPluginFT {

    private File projectDir
    private Project project
    private List<File> pluginClasspath

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())

        File source = Paths.get("src/functionalTest/resources/sealion-java-hello-world").toFile()
        Path targetPath = Paths.get("build/functionalTest/parent/sealion-java-hello-world")
        projectDir = Files.createDirectories(targetPath).toFile()
        FileUtils.copyDirectory(source, projectDir)

        project = ProjectBuilder.builder().withProjectDir(projectDir).build()

        // Skip tests that cannot connect to sonarqube
        Properties properties = new Properties()
        try {
            properties.load(Files.newInputStream(targetPath.resolve("gradle.properties")))
        } catch(Exception e) {
            // ignore
        }
        def sonarProperty = properties['systemProp.sonar.host.url']
        if (sonarProperty != null) {
            URL u = new URL(sonarProperty)
            try {
                HttpURLConnection huc = u.openConnection()
                huc.requestMethod = "GET"
                huc.connect()
                def response = huc.responseCode
                assumeTrue(response < 400)
            } catch(Exception e) {
                assumeNoException(e)
            }
        }
    }

    @Test
    void doesRunGradleBuildWithSuccess() {
        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
                .withNexusProperties()
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("clean", "build")
                .build()

        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.helloworld:build").getOutcome())
    }

    @Test
    void doesRunGradleAnalyzeBuildWithSuccess() {

        BuildResult result = SeasideGradleRunner.create().withProjectDir(projectDir)
                .withNexusProperties()
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments("analyze")
                .build()

        Assert.assertEquals(TaskOutcome.valueOf("SUCCESS"), result.task(":service.helloworld:analyze").getOutcome())
    }
}
