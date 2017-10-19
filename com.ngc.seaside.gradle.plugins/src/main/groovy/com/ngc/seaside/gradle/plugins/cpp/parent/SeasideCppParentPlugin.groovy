package com.ngc.seaside.gradle.plugins.cpp.parent

import com.ngc.seaside.gradle.plugins.parent.SeasideParentPlugin
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.tasks.cpp.dependencies.BuildingExtension
import com.ngc.seaside.gradle.tasks.cpp.dependencies.StaticBuildConfiguration
import com.ngc.seaside.gradle.tasks.cpp.dependencies.UnpackCppDistributionsTask
import com.ngc.seaside.gradle.tasks.dependencies.DownloadDependenciesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.resolve.ProjectModelResolver
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.cpp.tasks.CppCompile
import org.gradle.nativeplatform.NativeLibrarySpec
import org.gradle.nativeplatform.PrebuiltLibraries
import org.gradle.nativeplatform.test.tasks.RunTestExecutable
import org.gradle.nativeplatform.toolchain.Gcc
import org.gradle.nativeplatform.toolchain.VisualCpp
import org.gradle.platform.base.BinaryContainer

import java.util.regex.Matcher

/**
 * The Seaside wrapper for the Native plugin in Gradle.
 * This sets common attributes such as google-test-test-suite and provides a mechanism for unpacking
 * dependencies.
 *
 * The dependencies must have the following structure within a zip file.
 * <pre>
 * ${artifactId}-${version}.zip
 *   - include
 *      - ${artifactId}
 *        - *.h
 *   - lib
 *      - ${os}_${arch}
 *        - *.so / *.dll
 *        - *.a / *.lib
 *  </pre>
 *  <br>Example: <br>
 *  <pre>
 *   celix-2.0.0.zip
 *     include
 *       celix
 *         *.h
 *     lib
 *       linux_x86_64
 *         *.a
 *         *.so
 *
 *  </pre>
 *
 *  The configuration {@link BuildingExtension} allows for different library configurations.
 *  Dependencies that have the artifactId and the library as the same name don't have to specify the
 *  libs option for the shared or statically configurations.
 */
class SeasideCppParentPlugin implements Plugin<Project> {

    public static final String ANALYZE_TASK_NAME = 'analyze'

    @Override
    void apply(Project p) {
        p.configure(p) {

            project.extensions.create("building", BuildingExtension, p)

            applyPlugins(p)
            createTasks(p)

            configurations {
                compile
                testCompile
                distribution
            }

            task('copyCompileDependencies', type: Copy) {
                from configurations.compile
                into { "${project.buildDir}/dependencies" }
            }

            task('copyTestCompileDependencies', type: Copy) {
                from configurations.testCompile
                into { "${project.buildDir}/testDependencies" }
            }

            task('unpackCompileDependencies', type: UnpackCppDistributionsTask, dependsOn: copyCompileDependencies) {
                componentName = 'main'
                componentSourceSetName = 'cpp'
            }

            task('unpackTestCompileDependencies', type: UnpackCppDistributionsTask,
                 dependsOn: copyTestCompileDependencies) {
                testDependencies = true
            }

            task('copyExportedHeaders', type: Copy) {
                from 'src/main/include'
                into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/include" }
            }

            task('copySharedLib', type: Copy) {
                from "${project.buildDir}/libs/main/shared"
                into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/lib/" }
                rename { name ->
                    Matcher m = name.toString() =~ /main\.(.*)/
                    if (m.matches()) {
                        return "${project.group}.${project.name}-${project.version}." + m.group(1)
                    }
                    return null
                }
            }

            task('copyStaticLib', type: Copy) {
                from "${project.buildDir}/libs/main/static"
                into { "${project.distsDir}/${project.group}.${project.name}-${project.version}/lib/" }
                rename { name ->
                    Matcher m = name.toString() =~ /main\.(.*)/
                    if (m.matches()) {
                        return "${project.group}.${project.name}-${project.version}." + m.group(1)
                    }
                    return null
                }
            }

            task('createDistributionZip', type: Zip, dependsOn: [copyExportedHeaders, copySharedLib, copyStaticLib]) {
                from { "${project.distsDir}/${project.group}.${project.name}-${project.version}" }
            }

            afterEvaluate {

                repositories {
                    mavenLocal()

                    maven {
                        url nexusConsolidated
                    }
                }

                ext {
                    // The default name of the bundle.
                    bundleName = "$group" + '.' + "$project.name"
                }

                model {
                    repositories {
                        libs(PrebuiltLibraries) {
                        }
                    }

                    platforms {
                        windows_x86_64 {
                            operatingSystem 'windows'
                            architecture 'x64'
                        }
                        cygwin_x86_64 {
                            operatingSystem "windows"
                            architecture "x64"
                        }
                        linux_x86_64 {
                            operatingSystem 'linux'
                            architecture 'x64'
                        }
                    }

                    toolChains {
                        visualCpp(VisualCpp) {
                            eachPlatform {
                                linker.withArguments { args ->
                                    filterLinkerArgs(p.extensions.building, args)
                                }
                            }
                        }
                        gcc(Gcc) {
                            eachPlatform {
                                linker.withArguments { args ->
                                    filterLinkerArgs(p.extensions.building, args)
                                }
                            }
                        }
                    }

                    components {
                        main(NativeLibrarySpec) {
                            baseName = "${project.name}"
                            sources {
                                cpp {
                                    source {
                                        srcDirs 'src/main/cpp'
                                    }
                                    exportedHeaders {
                                        srcDirs 'src/main/include'
                                    }
                                }
                            }
                        }

                        all {
                            ["windows_x86_64", "cygwin_x86_64", "linux_x86_64"].each {
                                targetPlatform it
                            }
                        }
                    }
                }

                artifacts {
                    distribution createDistributionZip
                }

                sonarqube {
                    properties {
                        property 'sonar.cxx.coverage.reportPath', ["${project.buildDir}/lcov/coverage.xml"]
                        property 'sonar.cxx.xunit.reportPath', ["${project.buildDir}/test-results/**report.xml"]
                        property 'sonar.branch', SeasideParentPlugin.getBranchName()
                        property 'sonar.cxx.compiler.reportPath', ["${project.buildDir}/*.log"]
                        property 'sonar.projectName', "${bundleName}"
                        property 'sonar.cxx.compiler.parser', "GCC"
                        property 'sonar.cxx.compiler.charset', "UTF-8"
                        property 'sonar.cxx.compiler.regex', '=^(.*):([0-9]+):[0-9]+: warning: (.*)\\[(.*)\\]$'
                    }
                }

                tasks.withType(RunTestExecutable) {
                    args "--gtest_output=xml:report.xml"
                }

                tasks.getByName(
                        'createDistributionZip').archiveName = "${project.name}-${project.version}.zip"

                tasks.getByName('copySharedLib').onlyIf { file("${project.buildDir}/libs/main/shared").isDirectory() }
                tasks.getByName('copyStaticLib').onlyIf { file("${project.buildDir}/libs/main/static").isDirectory() }

                tasks.getByName('unpackCompileDependencies').dependenciesDirectory =
                        file("${project.buildDir}/dependencies")
                tasks.getByName('unpackTestCompileDependencies').dependenciesDirectory =
                        file("${project.buildDir}/testDependencies")
                tasks.withType(CppCompile, { task ->
                    task.dependsOn([unpackCompileDependencies, unpackTestCompileDependencies])
                })

                def binaries = p.getServices()
                        .get(ProjectModelResolver)
                        .resolveProjectModel(p.path)
                        .find('binaries', BinaryContainer)
                        .findAll { b -> b.buildable }
                tasks.getByName('copySharedLib').dependsOn(binaries)
                tasks.getByName('copyStaticLib').dependsOn(binaries)
            }
        }
    }


    /**
     * This method will search the already defined linker arguments and wrap any static libraries that have
     * been specified in the 'building' configuration with the 'withArgs' option
     *
     * @param buildingExtension the building configuration
     * @param linkerArgs the already existing linker arguments. This method will mutate this parameter by
     *                          adding arguments.
     */
    private void filterLinkerArgs(BuildingExtension buildingExtension, List<String> linkerArgs) {
        for (String file : buildingExtension.getStorage().getFilesWithLinkerArgs()) {
            if (linkerArgs.contains(file)) {
                int index = linkerArgs.indexOf(file)
                StaticBuildConfiguration.WithArgs withArgs = buildingExtension.storage.getLinkerArgs(file)
                linkerArgs.addAll(index, withArgs.before)
                linkerArgs.addAll(index + 1 + (withArgs.before.size()), withArgs.after)
            }
        }
    }

    protected void createTasks(Project project) {

        /**
         * analyzeBuild task for sonarqube
         */
        def buildTask = project.tasks.getByName("build")
        def sonarqubeTask = project.tasks.getByName("sonarqube")
        project.task(ANALYZE_TASK_NAME) {
        }
        project.tasks.getByName(ANALYZE_TASK_NAME).setGroup(project.getGroup())
        project.tasks.getByName(ANALYZE_TASK_NAME).dependsOn([buildTask, sonarqubeTask])
        project.tasks.getByName(ANALYZE_TASK_NAME).setDescription('Runs build and sonarqube')
    }

    /**
     * This plugin requires the java and maven plugins
     * @param project
     */
    protected void applyPlugins(Project project) {
        project.getPlugins().apply('cpp')
        project.getPlugins().apply('maven')
        project.getPlugins().apply('google-test-test-suite')
        project.getPlugins().apply('org.sonarqube')
    }
}
