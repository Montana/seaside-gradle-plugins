package com.ngc.seaside.gradle.plugins.eclipse.updatesite;

import com.ngc.seaside.gradle.plugins.eclipse.AbstractEclipseApplicationTask;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskExecutionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Task for publishing a category for an Eclipse update site.
 */
public class PublishCategoryTask extends AbstractEclipseApplicationTask<PublishCategoryTask> {

   private final DirectoryProperty updateSiteDirectory;
   private final RegularFileProperty category;

   /**
    * Constructor.
    */
   public PublishCategoryTask() {
      super(PublishCategoryTask.class);
      this.updateSiteDirectory = newOutputDirectory();
      this.category = newInputFile();
      getArgumentProviders().add(() -> Arrays.asList("-nosplash", "-application",
               "org.eclipse.equinox.p2.publisher.CategoryPublisher", "-compress", "-metadataRepository",
               getUpdateSiteDirUrl().get().toString(), "-artifactRepository", getUpdateSiteDirUrl().get().toString(),
               "-categoryDefinition", getCategoryUrl().get().toString(), "-categoryQualifier"));
   }

   /**
    * Returns the property of the category.xml file.
    * 
    * @return the property of the category.xml file
    */
   @InputFile
   public RegularFileProperty getCategory() {
      return category;
   }

   /**
    * Returns the property of the directory for the update site.
    * 
    * @return the property of the directory for the update site
    */
   @OutputDirectory
   public DirectoryProperty getUpdateSiteDirectory() {
      return updateSiteDirectory;
   }

   private Provider<URL> getUpdateSiteDirUrl() {
      return updateSiteDirectory.map(file -> file.getAsFile().toURI()).map(uri -> {
         try {
            return uri.toURL();
         } catch (MalformedURLException e) {
            throw new TaskExecutionException(this, e);
         }
      });
   }

   private Provider<URL> getCategoryUrl() {
      return category.map(file -> file.getAsFile().toURI()).map(uri -> {
         try {
            return uri.toURL();
         } catch (MalformedURLException e) {
            throw new TaskExecutionException(this, e);
         }
      });
   }
}