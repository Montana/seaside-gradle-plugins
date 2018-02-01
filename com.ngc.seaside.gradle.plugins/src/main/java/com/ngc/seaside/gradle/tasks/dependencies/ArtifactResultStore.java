package com.ngc.seaside.gradle.tasks.dependencies;

import com.google.common.base.Preconditions;

import org.eclipse.aether.resolution.ArtifactResult;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArtifactResultStore {

   private final Map<ArtifactKey, ArtifactResultRecord> mainResults = new HashMap<>();
   private final Path localRepositoryPath;
   private final Path outputDirectoryPath;

   public ArtifactResultStore(Path localRepositoryPath, Path outputDirectoryPath) {
      this.localRepositoryPath = localRepositoryPath;
      this.outputDirectoryPath = outputDirectoryPath;
   }

   public ArtifactResultStore addResult(ArtifactResult result, Path pom) {
      Preconditions.checkNotNull(result, "result may not be null!");
      Preconditions.checkNotNull(pom, "pom may not be null!");

      ArtifactResultRecord record = mainResults.computeIfAbsent(key(result),
                                                                k -> new ArtifactResultRecord(pom));
      if (isMainArtifact(result)) {
         record.mainArtifact = result;
      } else {
         record.additionalClassifiers.put(key(result)
                                                .setClassifier(result.getArtifact().getClassifier())
                                                .setExtension(result.getArtifact().getExtension()),
                                          result);
      }

      return this;
   }

   public ArtifactResultStore finish() {
      for (ArtifactResultRecord record : mainResults.values()) {
         if (record.mainArtifact == null) {
            // Pick a classifier to be the main artifact.
            Iterator<ArtifactResult> i = record.additionalClassifiers.values().iterator();
            record.mainArtifact = i.next();
            i.remove();
         }
      }
      return this;
   }

   public List<ArtifactResult> getMainResults() {
      return mainResults.values()
            .stream()
            .filter(r -> r.mainArtifact != null)
            .map(r -> r.mainArtifact)
            .collect(Collectors.toList());
   }

   public Path getRelativePathToMainArtifact(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return outputRelativePath(result);
   }

   public List<Path> getRelativePathsToOtherClassifiers(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .values()
            .stream()
            .map(this::outputRelativePath)
            .collect(Collectors.toList());
   }

   public Path getRelativePathToPom(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      Path pom = mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD).pom;
      Preconditions.checkState(pom != null, "unable to find POM for %s!", result.getArtifact().getFile());
      return outputRelativePath(pom);
   }

   public String getMainClassifier(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      ArtifactResult main = mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD).mainArtifact;
      Preconditions.checkState(main != null, "unable to find main artifact for %s!", result.getArtifact().getFile());
      return main.getArtifact().getClassifier();
   }

   public List<String> getOtherClassifiers(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .values()
            .stream()
            .map(r -> r.getArtifact().getClassifier())
            .collect(Collectors.toList());
   }

   public String getMainExtension(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      ArtifactResult main = mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD).mainArtifact;
      Preconditions.checkState(main != null, "unable to find main artifact for %s!", result.getArtifact().getFile());
      return main.getArtifact().getExtension();
   }

   public List<String> getOtherExtensions(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      return mainResults.getOrDefault(key(result), ArtifactResultRecord.EMPTY_RECORD)
            .additionalClassifiers
            .values()
            .stream()
            .map(r -> r.getArtifact().getExtension())
            .collect(Collectors.toList());
   }

   public boolean hasOtherClassifiers(ArtifactResult result) {
      Preconditions.checkNotNull(result, "result may not be null!");
      ArtifactResultRecord record = mainResults.get(key(result));
      return record != null && !record.additionalClassifiers.isEmpty();
   }

   Path outputRelativePath(Path file) {
      Path repoRelativePath = localRepositoryPath.relativize(file);
      return outputDirectoryPath.resolve(repoRelativePath);
   }

   private Path outputRelativePath(ArtifactResult result) {
      return outputRelativePath(result.getArtifact().getFile().toPath());
   }

   private static boolean isMainArtifact(ArtifactResult result) {
      return "".equals(result.getArtifact().getClassifier());
   }

   private static ArtifactKey key(ArtifactResult result) {
      return new ArtifactKey(result.getArtifact().getGroupId(),
                             result.getArtifact().getArtifactId(),
                             result.getArtifact().getVersion());
   }

   private static class ArtifactResultRecord {

      final static ArtifactResultRecord EMPTY_RECORD = new ArtifactResultRecord(null);

      final Map<ArtifactKey, ArtifactResult> additionalClassifiers = new LinkedHashMap<>();
      final Path pom;
      ArtifactResult mainArtifact;

      ArtifactResultRecord(Path pom) {
         this.pom = pom;
      }
   }
}
