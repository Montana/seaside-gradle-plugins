package com.ngc.seaside.gradle.tasks.dependencies;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactResultStoreTest {

   private ArtifactResultStore store;

   private Path localRepositoryPath = Paths.get("my", "m2", "repo");

   private Path outputDirectoryPath = Paths.get("some", "output");

   private Path pom;

   @Before
   public void setup() throws Throwable {
      pom = localRepositoryPath.resolve(Paths.get("foo", "bar.pom"));
      store = new ArtifactResultStore(localRepositoryPath, outputDirectoryPath);
   }

   @Test
   public void testDoesGetRelativePathToMainArtifact() throws Throwable {
      ArtifactResult result = newResult("foo",
                                        "bar",
                                        "1.0",
                                        null,
                                        "jar");

      store.addResult(result, pom);
      store.finish();
      assertTrue("did not add result to store!",
                 store.getMainResults().contains(result));
      assertEquals("did not return path to main artifact!",
                   outputRelativePath(result),
                   store.getRelativePathToMainArtifact(result));
      assertFalse("should not have other classifiers!",
                  store.hasOtherClassifiers(result));
      assertEquals("did not get main extension!",
                   "jar",
                   store.getMainExtension(result));
      assertEquals("did not get main classifier!",
                   "",
                   store.getMainClassifier(result));
   }

   @Test
   public void testDoesGetRelativePathToPom() throws Throwable {
      ArtifactResult result = newResult("foo",
                                        "bar",
                                        "1.0",
                                        null,
                                        "jar");
      store.addResult(result, pom);
      store.finish();
      assertEquals("did not return path to main pom!",
                   outputRelativePath(pom),
                   store.getRelativePathToPom(result));
   }

   @Test
   public void testDoesGetOtherClassifiers() throws Throwable {
      ArtifactResult mainResult = newResult("foo",
                                            "bar",
                                            "1.0",
                                            null,
                                            "jar");
      ArtifactResult sourcesResult = newResult("foo",
                                               "bar",
                                               "1.0",
                                               "sources",
                                               "jar");

      store.addResult(mainResult, pom);
      store.addResult(sourcesResult, pom);
      store.finish();
      assertTrue("did not add result to store!",
                 store.getMainResults().contains(mainResult));
      assertFalse("should not return sources as a main result!",
                  store.getMainResults().contains(sourcesResult));
      assertEquals("did not return path to main artifact!",
                   outputRelativePath(mainResult),
                   store.getRelativePathToMainArtifact(mainResult));
      assertTrue("should have other classifiers!",
                 store.hasOtherClassifiers(mainResult));
      assertEquals("did not return sources classifier!",
                   Collections.singletonList("sources"),
                   store.getOtherClassifiers(mainResult));
   }

   @Test
   public void testDoesGetOtherExtensions() throws Throwable {
      ArtifactResult mainResult = newResult("foo",
                                            "bar",
                                            "1.0",
                                            null,
                                            "jar");
      ArtifactResult sourcesResult = newResult("foo",
                                               "bar",
                                               "1.0",
                                               "sources",
                                               "jar");

      store.addResult(mainResult, pom);
      store.addResult(sourcesResult, pom);
      store.finish();
      assertEquals("did not return jar extension for sources classifier!",
                   Collections.singletonList("jar"),
                   store.getOtherExtensions(mainResult));
   }

   @Test
   public void testDoesGetRelativePathsToOtherClassifiers() throws Throwable {
      ArtifactResult mainResult = newResult("foo",
                                            "bar",
                                            "1.0",
                                            null,
                                            "jar");
      ArtifactResult sourcesResult = newResult("foo",
                                               "bar",
                                               "1.0",
                                               "sources",
                                               "jar");
      store.addResult(mainResult, pom);
      store.addResult(sourcesResult, pom);
      store.finish();
      assertEquals("did not return jar extension for sources classifier!",
                   Collections.singletonList(outputRelativePath(sourcesResult)),
                   store.getRelativePathsToOtherClassifiers(mainResult));
   }

   @Test
   public void testDoesHandleClassifierOnlyArtifact() throws Throwable {
      ArtifactResult sourcesResult = newResult("foo",
                                               "bar",
                                               "1.0",
                                               "sources",
                                               "jar");

      store.addResult(sourcesResult, pom);
      store.finish();

      assertTrue("did not return classifier only as main result",
                 store.getMainResults().contains(sourcesResult));
      assertEquals("did not get main extension!",
                   "jar",
                   store.getMainExtension(sourcesResult));
      assertEquals("did not get main classifier!",
                   "sources",
                   store.getMainClassifier(sourcesResult));
   }

   private ArtifactResult newResult(String groupId,
                                    String artifactId,
                                    String version,
                                    String classifier,
                                    String extension) {
      Artifact artifact = mock(Artifact.class);
      when(artifact.getGroupId()).thenReturn(groupId);
      when(artifact.getArtifactId()).thenReturn(artifactId);
      when(artifact.getVersion()).thenReturn(version);
      when(artifact.getClassifier()).thenReturn(classifier == null ? "" : classifier);
      when(artifact.getExtension()).thenReturn(extension);
      when(artifact.getFile()).thenReturn(new File(
            localRepositoryPath.toFile(),
            String.format("%s/%s/%s/%s-%s%s.%s",
                          groupId,
                          artifactId,
                          version,
                          artifactId,
                          version,
                          classifier == null ? "" : "-" + classifier,
                          extension)));

      ArtifactResult artifactResult = new ArtifactResult(new ArtifactRequest());
      artifactResult.setArtifact(artifact);
      return artifactResult;
   }

   private Path outputRelativePath(ArtifactResult result) {
      return outputRelativePath(result.getArtifact().getFile().toPath());
   }

   private Path outputRelativePath(Path file) {
      Path repoRelativePath = localRepositoryPath.relativize(file);
      return outputDirectoryPath.resolve(repoRelativePath);
   }
}
