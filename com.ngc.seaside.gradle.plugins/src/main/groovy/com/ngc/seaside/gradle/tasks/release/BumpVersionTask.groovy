package com.ngc.seaside.gradle.tasks.release

import com.google.common.base.Preconditions
import com.ngc.seaside.gradle.plugins.release.SeasideReleasePlugin
import com.ngc.seaside.gradle.util.ReleaseUtil
import com.ngc.seaside.gradle.util.VersionResolver
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * Used to increment the version in the build.gradle file
 *   and then commit it to the repository
 */
class BumpVersionTask extends DefaultTask {

   //will be used in the future
   boolean dryRun

   boolean commitChanges
   boolean push

   private ReleaseType releaseType = ReleaseType.MINOR
   private String versionSuffix
   private VersionResolver resolver


   /**
    * CTOR
    *
    * @Inject was used because there are two constructors and gradle seems to be confused on which constructor to use.
    */
   @Inject
   BumpVersionTask() {
      resolver = new VersionResolver(project)
      resolver.enforceVersionSuffix = true
   }

   /**
    * CTOR used by testing framework
    * @param resolver
    * @param typeOfRelease defaulted to ReleaseType Minor
    * @param version suffix to be used
    */
   BumpVersionTask(VersionResolver resolver,
                   ReleaseType typeOfRelease = ReleaseType.MINOR,
                   String versionSuffix='-SNAPSHOT') {
      this.resolver = Preconditions.checkNotNull(resolver, "resolver may not be null!")
      this.releaseType = typeOfRelease
      this.versionSuffix = versionSuffix
   }

   /**
    * function required to be a task within the
    * gradle framework and is the entry point for
    * gradle
    *
    * @return
    */
   @TaskAction
   def bumpTheVersion() {
       getReleaseExtensionSettings()
       commitNextVersionToFile()
   }

   /**
    *
    * @return the string of the next version to be written to the build.gradle file
    */
   String setNextVersion() {
      return getCurrentVersion() + getVersionSuffix()
   }

   private String getVersionSuffix() {
      return versionSuffix
   }

   /**
    *
    * @return ReleaseType for this version
    */
   ReleaseType getReleaseType() {
      return releaseType
   }

   /**
    *
    * @return String based on the current version in the build.gradle file
    */
   private String getCurrentVersion() {
      return resolver.getProjectVersion()
   }

   /**
    *
    * @param nextVersion used to set the version in the build.gradle file
    */
   private void commitNextVersionToFile() {
      def nextVersion = setNextVersion()
      resolver.setProjectVersionOnFile(nextVersion)
      commitVersionFileWithMessage("Creating new $nextVersion version after release")
      logger.lifecycle("\n Updated project version to $nextVersion")
   }

   /**
    * commits new build.gradle to our GitHub repository
    *
    * @param msg to be used with the git command
    */
   private void commitVersionFileWithMessage(String msg) {
      project.exec ReleaseUtil.git("commit", "-m", "$msg", "$resolver.versionFile.absolutePath")

   }

   /**
    * Resolves class variables with the Project extension variables
    */
   private void getReleaseExtensionSettings() {
      commitChanges = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).commitChanges
      push = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).push
      versionSuffix = ReleaseUtil.getReleaseExtension(project, SeasideReleasePlugin.RELEASE_EXTENSION_NAME).versionSuffix
   }

}
