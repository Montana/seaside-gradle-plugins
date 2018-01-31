package com.ngc.seaside.gradle.plugins.release

import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class SeasideReleaseRootProjectPluginFT {
    private File projectDir
    private Project project
    private List<File> pluginClasspath
    private SeasideReleaseRootProjectPlugin plugin
    private List<String> projectNames = [
            "bonjourlemonde",
            "helloworld",
            "holamundo"
    ]
    private Map<String, List<String>> subprojectNames = new HashMap<String, List<String>>()

    SeasideReleaseRootProjectPluginFT() {
        subprojectNames.put(projectNames[0], ["bonjour", "aurevoir"])
        subprojectNames.put(projectNames[1], ["hello", "goodbye"])
        subprojectNames.put(projectNames[2], ["hola", "adios"])
    }

    @Before
    void before() {
        pluginClasspath = TestingUtilities.getTestClassPath(getClass())
        projectDir = TestingUtilities.setUpTheTestProjectDirectory(
            sourceDirectoryWithTheTestProject(),
            pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(projectDir)

        plugin = new SeasideReleaseRootProjectPlugin()
        plugin.apply(project)

        setupTestingGitRepo()
    }

    @After
    void after() {
        if (projectDir != null) {
            projectDir.deleteDir()
        }
    }

    @Test
    void doesRemoveVersionSuffix() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_REMOVE_VERSION_SUFFIX_TASK_NAME) {
            def output = new ByteArrayOutputStream()
            def result = project.exec ReleaseUtil.gitWithOutput(output, "log", "--pretty=format:%s")
            Assert.assertEquals(0, result.getExitValue())
            Assert.assertEquals(
                  "output did not contain expected release message!",
                  output.toString().split("\n")[0],
                  "Release of version v1.2.3"
            )
        }
    }

    @Ignore
    @Test
    void doesCreateTag() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_CREATE_TAG_TASK_NAME)
    }

    @Ignore
    @Test
    void doesBumpVersion() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_BUMP_VERSION_TASK_NAME)
    }

    @Ignore
    @Test
    void doesReleasePush() {
        checkForTaskSuccess(SeasideReleaseRootProjectPlugin.RELEASE_PUSH_TASK_NAME)
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath(
            "src", "functionalTest", "resources", "sealion-java-hello-world-monorepo"
        )
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath(
            "build", "functionalTest", "release", "sealion-java-hello-world-monorepo"
        )
    }

    private void setupTestingGitRepo() {
        project.exec ReleaseUtil.git("init", projectDir.getAbsolutePath())
        project.exec ReleaseUtil.git("add", ".")
        project.exec ReleaseUtil.git("commit", "-m", "initial commit")
    }

    private void checkForTaskSuccess(String taskName, Closure closure) {
        checkForTaskSuccess(taskName)
        closure.call()
    }

    private void checkForTaskSuccess(String taskName) {
        projectNames.each { projectName ->
            BuildResult result = GradleRunner.create()
                    .withProjectDir(TestingUtilities.turnListIntoPath(projectDir.toString(), projectName))
                    .withPluginClasspath(pluginClasspath)
                    .forwardOutput()
                    .withArguments("clean", "build", taskName)
                    .build()

            subprojectNames.get(projectName).each { subprojectName ->
                TestingUtilities.assertTaskSuccess(result, subprojectName, taskName)
            }
        }
    }
}
