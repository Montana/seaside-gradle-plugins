package com.ngc.seaside.gradle.plugins.eclipse.updatesite;

import com.ngc.seaside.gradle.plugins.eclipse.updatesite.category.EclipseCategory;
import com.ngc.seaside.gradle.plugins.eclipse.updatesite.feature.EclipseFeature;

import groovy.lang.Closure;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.io.File;

/**
 * Extension for the seaside eclipse update site plugin.
 */
public class SeasideEclipseUpdateSiteExtension {

   private final Project project;
   private final RegularFileProperty updateSiteArchive;
   private final SetProperty<EclipseFeature> features;
   private final SetProperty<EclipseCategory> categories;

   /**
    * Create an instance of the SeasideEclipseUpdateSiteExtension
    * 
    * @param project the project on which to create the extension
    */
   public SeasideEclipseUpdateSiteExtension(Project project) {
      this.project = project;
      this.updateSiteArchive = project.getLayout().fileProperty();
      this.updateSiteArchive.set(project.getLayout().getBuildDirectory().file(project.getProviders()
               .provider(() -> "updatesite/" + project.getGroup() + "." + project.getName() + "-" + project.getVersion()
                        + ".zip")));
      this.features = project.getObjects().setProperty(EclipseFeature.class);
      this.categories = project.getObjects().setProperty(EclipseCategory.class);
   }

   /**
    * Returns the property of the update site archive zip. By default this is {@code group.artifact-version.zip} in the
    * build directory.
    * 
    * @return the property of the update site archive zip
    */
   public RegularFileProperty getUpdateSiteArchive() {
      return updateSiteArchive;
   }

   /**
    * Returns the provider of the unzipped update site directory.
    * 
    * @return the provider of the unzipped update site directory
    */
   public Provider<Directory> getUpdateSiteDirectory() {
      return getUpdateSiteArchive().map(regularFile -> {
         File file = regularFile.getAsFile();
         String name = FilenameUtils.getBaseName(file.getName());
         DirectoryProperty dir = project.getLayout().directoryProperty();
         dir.set(file.getParentFile());
         return dir.dir(name).get();
      });
   }

   /**
    * Returns the propert of the features for the update site.
    * 
    * @return the propert of the features for the update site
    */
   public SetProperty<EclipseFeature> getFeatures() {
      return features;
   }

   /**
    * Creates a new feature and applies the given action to it.
    * 
    * @param action action for configuring the feature
    * @return the feature
    */
   public EclipseFeature feature(Action<EclipseFeature> action) {
      EclipseFeature feature = new EclipseFeature();
      action.execute(feature);
      this.features.add(feature);
      return feature;
   }

   /**
    * Creates a new feature and applies the given closure to it.
    * 
    * @param closure closure
    * @return the feature
    */
   public EclipseFeature feature(Closure<?> closure) {
      EclipseFeature feature = new EclipseFeature();
      closure.setDelegate(feature);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(feature);
      this.features.add(feature);
      return feature;
   }

   /**
    * Adds the given feature.
    * 
    * @param feature feature
    */
   public void feature(EclipseFeature feature) {
      this.features.add(feature);
   }

   /**
    * Returns the property of the categories for the update site.
    * 
    * @return the propert of the categories for the update site
    */
   public SetProperty<EclipseCategory> getCategories() {
      return categories;
   }

   /**
    * Creates a new category and applies the given action to it.
    * 
    * @param action action for configuring the category
    * @return the category
    */
   public EclipseCategory category(Action<EclipseCategory> action) {
      EclipseCategory category = new EclipseCategory();
      action.execute(category);
      this.categories.add(category);
      return category;
   }

   /**
    * Creates a new category and applies the given closure to it.
    * 
    * @param closure closure
    * @return the category
    */
   public EclipseCategory category(Closure<?> closure) {
      EclipseCategory category = new EclipseCategory();
      closure.setDelegate(category);
      closure.setResolveStrategy(Closure.DELEGATE_FIRST);
      closure.call(category);
      this.categories.add(category);
      return category;
   }

}
