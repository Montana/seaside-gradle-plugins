package com.ngc.seaside.gradle.tasks.dependencies;

import com.ngc.seaside.gradle.util.test.GradleMocks;

import org.eclipse.aether.resolution.DependencyResult;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newDependencyResult;
import static com.ngc.seaside.gradle.tasks.dependencies.AetherMocks.newLocalMavenRepo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CopyDependencyFilesActionIT {

   private CopyDependencyFilesAction action;

   private ProjectInternal project = GradleMocks.newProjectMock();

   @Mock
   private PopulateMaven2Repository task;

   @Rule
   public TemporaryFolder localRepositoryDirectory = new TemporaryFolder();

   @Rule
   public TemporaryFolder outputDirectory = new TemporaryFolder();

   private File jar;

   private File sources;

   private File tests;

   private File pom;

   @Before
   public void setup() throws Throwable {
      // Setup some files in the fake repo.
      File dir = localRepositoryDirectory.newFolder("a", "b", "1.0");
      jar = new File(dir, "b-1.0.jar");
      sources = new File(dir, "b-1.0-sources.jar");
      tests = new File(dir, "b-1.0-tests.jar");
      pom = new File(dir, "b-1.0.pom");
      jar.createNewFile();
      sources.createNewFile();
      tests.createNewFile();
      pom.createNewFile();

      Logger logger = mock(Logger.class);
      when(task.getProject()).thenReturn(project);
      when(task.getLogger()).thenReturn(logger);

      action = new CopyDependencyFilesAction();
   }

   @Test
   public void testDoesCopyDependencies() {
      DependencyResult jarResult = newDependencyResult(jar);
      DependencyResult sourcesResult = newDependencyResult(sources);
      DependencyResult testsResult = newDependencyResult(tests);

      MavenArtifactRepository local = newLocalMavenRepo(localRepositoryDirectory.getRoot());
      when(task.getOutputDirectory()).thenReturn(outputDirectory.getRoot());
      when(task.getLocalRepository()).thenReturn(local);

      action.setDependencyResults(Arrays.asList(jarResult, sourcesResult, testsResult));
      action.execute(task);

      assertTrue("did not copy JAR file!",
                 new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists());
      assertTrue("did not copy sources file!",
                 new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-sources.jar").exists());
      assertTrue("did not copy tests file!",
                 new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-tests.jar").exists());
      assertTrue("did not copy POM file!",
                 new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.pom").exists());
   }

   @Test
   public void testDoesNotCopyFilesIfDisabled() {
      DependencyResult jarResult = newDependencyResult(jar);
      DependencyResult sourcesResult = newDependencyResult(sources);
      DependencyResult testsResult = newDependencyResult(tests);

      MavenArtifactRepository local = newLocalMavenRepo(localRepositoryDirectory.getRoot());
      when(task.getOutputDirectory()).thenReturn(outputDirectory.getRoot());
      when(task.getLocalRepository()).thenReturn(local);
      when(task.isPopulateLocalRepoOnly()).thenReturn(true);

      action.setDependencyResults(Arrays.asList(jarResult, sourcesResult, testsResult));
      action.execute(task);

      assertFalse("should not have copied JAR file!",
                  new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists());
      assertFalse("should not have copied sources file!",
                  new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-sources.jar").exists());
      assertFalse("should not have copied tests file!",
                  new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0-tests.jar").exists());
      assertFalse("should not have copied POM file!",
                  new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.pom").exists());
   }

   @Test
   public void doesNotCopyFilesWithRelativePaths() {
      DependencyResult jarResult = newDependencyResult(new File(localRepositoryDirectory.getRoot(), ".."));

      MavenArtifactRepository local = newLocalMavenRepo(localRepositoryDirectory.getRoot());
      when(task.getOutputDirectory()).thenReturn(outputDirectory.getRoot());
      when(task.getLocalRepository()).thenReturn(local);
      when(task.isPopulateLocalRepoOnly()).thenReturn(true);

      action.setDependencyResults(Collections.singletonList(jarResult));
      action.execute(task);

      assertFalse("should not copy JAR file!",
                  new File(outputDirectory.getRoot(), "a/b/1.0/b-1.0.jar").exists());
   }
}
