package com.ngc.seaside.gradle.plugins.ci

import com.ngc.seaside.gradle.api.plugins.AbstractProjectPlugin
import com.ngc.seaside.gradle.extensions.ci.SeasideCiExtension
import com.ngc.seaside.gradle.tasks.dependencies.PopulateMaven2Repository
import com.ngc.seaside.gradle.util.PropertyUtils
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * This plugin is applied to projects to make CI easier.
 *
 * <p/>
 *
 * This plugin also supports the {@code display.property.name} system property.  If If set, the value of the build
 * script property will be displayed.  The value can be a comma delimited list of properties.  Each property will be
 * printed on a separate line.  This is useful for Jenkins scripts which want to capture the value of some property.
 * A task named {@code nothing} is configured that allows a script to invoke Gradle with the system properties but
 * no part of the build is actually performed.  For example,
 * <pre>
 *     fooVersion=$(gradle -q nothing -Ddisplay.property.name=version)
 * </pre>
 * If the plugin is applied to mutiple projects in the same build, use
 * <pre>
 *     fooVersion=$(gradle -q nothing -Ddisplay.property.name=version | head -1)
 * </pre>
 *
 * <p/>
 *
 * The system properties {@code update.property.name} and {@code update.property.value} can also be used to replace
 * build script properties before the build is started.  These values can be a comma delimited list of properties and
 * values.  The property names and values should be listed in the same order.  For example,
 * <pre>
 *     gradle clean build -Dupdate.property.name=version,barVersion -Dupdate.property.value=1.0-SNAPSHOT,2.3
 * </pre>
 *
 * <p/>
 *
 * This plugin also applies the {@link PopulateMaven2Repository} task.
 */
class SeasideCiPlugin extends AbstractProjectPlugin {

    public static final String JENKINS_TASK_GROUP_NAME = 'Jenkins'
    public static final String AUDITING_TASK_GROUP_NAME = 'Auditing'
    public static final String NOTHING_TASK_NAME = 'nothing'
    public static final String CREATE_M2_REPO_TASK_NAME = 'm2repo'

    /**
     * The name of the system property used when printing the value of a property.
     */
    private final static String DISPLAY_PROPERTY_NAME = 'display.property.name'

    /**
     * The name of the system property used when updating a property of the build.  The value may be a comma delimited
     * list of property names.
     */
    private final static String UPDATE_PROPERTY_NAME = 'update.property.name'

    /**
     * The name of the system property used when updating a property of the build.  The value may be a comma delimited
     * list of values for properties.
     */
    private final static String UPDATE_PROPERTY_VALUE = 'update.property.value'

    /**
     * The CI extension that the user can use to customize the plugin.
     */
    private SeasideCiExtension ciExtension

    @Override
    void doApply(Project project) {
        project.configure(project) {
            configureExtensions(project)
            createTasks(project)

            // This work is not done in a task because we what to do the property replacement before any actual work is
            // done, including dependency resolution.  Ensuring that a task gets run before anything else can be difficult,
            // so we do this work in a "beforeEvaluate" project callback.
            configurePropertyUpdate(project)
            // Configure the build to handle the system properties for updating the properties of the build.
            // Note the display property work could be done in task but the update property work cannot be (easily) done in
            // a task.  To be consistent, we don't do any of the work in a task.
            configurePropertyDisplay(project)
            // Configure the m2 repo task from values of the extension.
            configureCreateM2RepoTask(project)
        }
    }

    /**
     * Adds additional CI based tasks.
     */
    protected void createTasks(Project project) {
        Task task = project.task(NOTHING_TASK_NAME)
        task.enabled = false
        task.group = JENKINS_TASK_GROUP_NAME
        task.description = 'Does nothing.  This task is useful when invoked from a Jenkins script since it allows scripts to capture the values of build script properties without doing actual work.'

        project.task(CREATE_M2_REPO_TASK_NAME,
                     type: PopulateMaven2Repository,
                     group: AUDITING_TASK_GROUP_NAME,
                     description: 'Creates a directory which contains all dependencies in a maven2 layout which can be used for offline use.') {
            localRepository = mavenLocal()
        }
    }

    /**
     * Configures extensions for the plugin.
     * @param project
     */
    private void configureExtensions(Project project) {
        ciExtension = project.extensions.create("seasideCi", SeasideCiExtension)
    }

    /**
     * Configures the task to create the M2 repository.  Applies the configuration after the project has been evaluated
     * so the user has a chance to set the extensions.
     */
    private void configureCreateM2RepoTask(Project project) {
        // Configure the task with values from the extension after the project is evaluated so the user has a chance
        // to override the settings.
        project.afterEvaluate {
            getTaskResolver().findTask(CREATE_M2_REPO_TASK_NAME) {
                // Note findByName returns null if the repo could not be found.  It is okay if the repo is not
                // defined.  In this case, the task will just resolve dependencies from the local maven repository
                // directory.
                remoteRepository = project.repositories.findByName(ciExtension.remoteM2RepositoryName)
                // Configure the output directory using $buildDir/m2 as the default.
                outputDirectory = ciExtension.m2OutputDirectory ?: new File(project.buildDir, 'm2')
            }
        }
    }

    /**
     * Sets up a callback that is invoked before the project is update.d  The callback will display the values of all
     * properties named via the {@link #DISPLAY_PROPERTY_NAME} system property.
     * @param project
     */
    private static void configurePropertyDisplay(Project project) {
        project.afterEvaluate {
            String displayPropertyName = System.getProperty(DISPLAY_PROPERTY_NAME)
            if (displayPropertyName != null) {
                PropertyUtils.getProperties(project, displayPropertyName).forEach({ v ->
                    // Use the quiet log level so that the output is always printed.  This makes parsing the output
                    // within a shell script easier.
                    project.logger.quiet(v.toString())
                })
            }
        }
    }

    /**
     * Sets up a callback that is invoked before the project is updated.  The callback will update any properties
     * configured for the build if the {@link #UPDATE_PROPERTY_NAME} and {@link #UPDATE_PROPERTY_VALUE} system
     * properties are set.
     */
    private static void configurePropertyUpdate(Project project) {
        project.beforeEvaluate {
            String updatePropertyName = System.getProperty(UPDATE_PROPERTY_NAME)
            String updatePropertyValue = System.getProperty(UPDATE_PROPERTY_VALUE)
            if (updatePropertyName != null && updatePropertyValue != null) {
                PropertyUtils.setProperties(project, updatePropertyName, updatePropertyValue)
            }
        }
    }
}
