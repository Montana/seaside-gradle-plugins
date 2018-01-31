package com.ngc.seaside.gradle.api.plugins

import com.ngc.seaside.gradle.tasks.release.ReleaseType
import com.ngc.seaside.gradle.util.TaskResolver
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.Project

/**
 * This base class for a project project plugin implementation is used to
 * ensure that the project version setting remains the same amongst all plugins.
 * This also allows plugins to be implemented with knowledge that the project.version is set correctly.
 */
abstract class AbstractProjectPlugin implements IProjectPlugin {
    static final String VERSION_SETTINGS_CONVENTION_NAME = 'versionSettings'

    private VersionResolver versionResolver
    private TaskResolver taskResolver

    /**
     * Inject project version configuration and force subclasses to use it
     * @param project project applying this plugin
     */
    @Override
    final void apply(Project project) {
        taskResolver = new TaskResolver(project)
        if (project.extensions.findByName(VERSION_SETTINGS_CONVENTION_NAME) == null) {
            versionResolver = project.extensions.create(VERSION_SETTINGS_CONVENTION_NAME, VersionResolver, project)
        } else {
            versionResolver = (VersionResolver)project.extensions.findByName(VERSION_SETTINGS_CONVENTION_NAME)
        }
        versionResolver.setEnforceVersionSuffix(false)
        project.version = "${-> versionResolver.getUpdatedProjectVersionForRelease(ReleaseType.SNAPSHOT)}"
        doApply(project)
    }

    @Override
    TaskResolver getTaskResolver() {
        return taskResolver
    }

    @Override
    VersionResolver getVersionResolver() {
        return versionResolver
    }
}
