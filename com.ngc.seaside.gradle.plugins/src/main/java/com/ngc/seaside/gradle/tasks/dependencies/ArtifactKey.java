package com.ngc.seaside.gradle.tasks.dependencies;

import java.util.Objects;

public class ArtifactKey {

   private final String groupId;
   private final String artifactId;
   private final String version;
   private String classifier;
   private String extension;

   public ArtifactKey(String groupId, String artifactId, String version) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
   }

   public String getGroupId() {
      return groupId;
   }

   public String getArtifactId() {
      return artifactId;
   }

   public String getVersion() {
      return version;
   }

   public String getClassifier() {
      return classifier;
   }

   public ArtifactKey setClassifier(String classifier) {
      this.classifier = classifier;
      return this;
   }

   public String getExtension() {
      return extension;
   }

   public ArtifactKey setExtension(String extension) {
      this.extension = extension;
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof ArtifactKey)) {
         return false;
      }
      ArtifactKey that = (ArtifactKey) o;
      return Objects.equals(groupId, that.groupId) &&
             Objects.equals(artifactId, that.artifactId) &&
             Objects.equals(version, that.version) &&
             Objects.equals(classifier, that.classifier) &&
             Objects.equals(extension, that.extension);
   }

   @Override
   public int hashCode() {
      return Objects.hash(groupId, artifactId, version, classifier, extension);
   }
}
