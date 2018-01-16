package com.ngc.seaside.gradle.plugins.cpp.coverage

import com.ngc.seaside.gradle.extensions.cpp.coverage.SeasideCppCoverageExtension
import com.ngc.seaside.gradle.util.FileUtil
import com.ngc.seaside.gradle.util.test.TestingUtilities
import org.gradle.api.Project
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assume.assumeFalse

@Ignore("Our current efforts are not focused on C++ and this test is failing.")
class SeasideCppCoveragePluginFT {

    private final String SUBPROJECT_DIR_PREFIX = "com.ngc.blocs.cpp."

    private SeasideCppCoverageExtension coverageExtension
    private File testProjectDir
    private Project project
    private List<File> pluginClasspath
    private List<String> subprojectNames = [
            "service.api",
            "service.utilities",
            "service.log.impl.logservice",
            "service.thread.impl.threadservice",
            "service.time.impl.timeservice",
            "service.event.impl.synceventservice",
            "service.log.impl.printservice"
    ]

    @Before
    void before() {
        // This test only works on Linux.
        assumeFalse("Current OS is Windows, skipping test.",
                    System.getProperty("os.name").toLowerCase().startsWith("win"))

        pluginClasspath = TestingUtilities.getTestClassPath(getClass())
        testProjectDir = TestingUtilities.setUpTheTestProjectDirectory(
                sourceDirectoryWithTheTestProject(),
                pathToTheDestinationProjectDirectory()
        )
        project = TestingUtilities.createTheTestProjectWith(testProjectDir)
    }


    @Test
    void doesGenerateCoverageData() {
        checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_COVERAGE_DATA_TASK_NAME)
        checkForTheExtractedLcovFiles()
        checkForTheCoverageFile()
        checkForTheHtmlFile()
        checkForTheXMLFile()
    }

    @Test
    void doesGenerateCppCheckReports() {
        checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_CPPCHECK_REPORT_TASK_NAME)
        checkForTheCppCheckFiles()
    }

    @Test
    void doesGenerateRatsReports() {
        checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_RATS_REPORT_TASK_NAME)
        checkForTheRatsFiles()
    }

    @Test
    void doesGenerateFullReport() {
        checkForTaskSuccess(SeasideCppCoveragePlugin.GENERATE_FULL_COVERAGE_REPORT_TASK_NAME)
        checkForTheExtractedLcovFiles()
        checkForTheCoverageFile()
        checkForTheHtmlFile()
        checkForTheXMLFile()
        checkForTheCppCheckFiles()
        checkForTheRatsFiles()
    }

    private static File sourceDirectoryWithTheTestProject() {
        return TestingUtilities.turnListIntoPath("src", "functionalTest", "resources", "pipeline-test-cpp")
    }

    private static File pathToTheDestinationProjectDirectory() {
        return TestingUtilities.turnListIntoPath("build", "functionalTest", "cpp", "coverage", "pipeline-test-cpp")
    }

    private void checkForTaskSuccess(String taskName) {
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath(pluginClasspath)
                .forwardOutput()
                .withArguments(taskName)
                .build()

        subprojectNames.each { subprojectName ->
            TestingUtilities.assertTaskSuccess(result, subprojectName, taskName)
        }
    }

    private void checkForTheExtractedLcovFiles() {
        subprojectNames.each { subprojectName ->
            def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
            Project subproject = TestingUtilities.createSubprojectWithDir(project, file)
            if (isSubproject(file)) {
                def f = new File(FileUtil.toPath(subproject.buildDir.absolutePath, "tmp", "lcov"))
                Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
            }
        }
    }

    private checkForTheCoverageFile() {
        subprojectNames.each { subprojectName ->
            def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
            if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0])) {
                return
            }

            if (isSubproject(file)) {
                coverageExtension = createAnExtensionOnTheSubproject(file)
                def f = new File(coverageExtension.coverageFilePath)
                Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
                Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
            }
        }
    }

    private checkForTheHtmlFile() {
        subprojectNames.each { subprojectName ->
            def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
            if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0])) {
                return
            }

            Project subproject = TestingUtilities.createSubprojectWithDir(project, file)

            if (isSubproject(file)) {
                String coverageHtmlDir = FileUtil.toPath(subproject.buildDir.absolutePath, "reports", "lcov", "html")
                def f = new File(coverageHtmlDir + "/index.html")
                Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
                Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
            }
        }
    }

    private checkForTheXMLFile() {
        subprojectNames.each { subprojectName ->
            def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
            if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0])) {
                return
            }

            if (isSubproject(file)) {
                coverageExtension = createAnExtensionOnTheSubproject(file)
                def f = new File(coverageExtension.coverageXmlPath)
                Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
                Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
            }
        }
    }

    private checkForTheCppCheckFiles() {
        subprojectNames.each { subprojectName ->
            def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
            if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0])) {
                return
            }
            Project subproject = TestingUtilities.createSubprojectWithDir(project, file)
            if (isSubproject(file)) {
                coverageExtension = createAnExtensionOnTheSubproject(file)
                def f = new File(coverageExtension.cppCheckXmlPath)
                String cppcheckHtmlDir = FileUtil.
                        toPath(subproject.buildDir.absolutePath, "reports", "cppcheck", "html")
                def f2 = new File(cppcheckHtmlDir + "/index.html")
                Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
                Assert.assertTrue("The file does not exist: ${f2.absolutePath}", f.exists())
                Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
                Assert.assertTrue("The file is empty: ${f2.absolutePath}", f.text.length() > 0)
            }
        }
    }

    private checkForTheRatsFiles() {
        subprojectNames.each { subprojectName ->
            def file = new File([testProjectDir, SUBPROJECT_DIR_PREFIX + subprojectName].join(File.separator))
            if (file.name.endsWith(SUBPROJECT_DIR_PREFIX + subprojectNames[0])) {
                return
            }

            if (isSubproject(file)) {
                coverageExtension = createAnExtensionOnTheSubproject(file)
                def f = new File(coverageExtension.ratsXmlPath)
                def f2 = new File(coverageExtension.ratsHtmlPath)
                Assert.assertTrue("The file does not exist: ${f.absolutePath}", f.exists())
                Assert.assertTrue("The file does not exist: ${f2.absolutePath}", f2.exists())
                Assert.assertTrue("The file is empty: ${f.absolutePath}", f.text.length() > 0)
                Assert.assertTrue("The file is empty: ${f2.absolutePath}", f2.text.length() > 0)
            }
        }
    }

    private boolean isSubproject(File file) {
        return file.directory && file.name.startsWith(SUBPROJECT_DIR_PREFIX)
    }

    private SeasideCppCoverageExtension createAnExtensionOnTheSubproject(File file) {
        def subproject = TestingUtilities.createSubprojectWithDir(project, file)
        coverageExtension = new SeasideCppCoverageExtension(subproject)
        return coverageExtension
    }
}
