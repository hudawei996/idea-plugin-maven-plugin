/**
 *
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package b2s.maven.idea.plugin.packaging;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import zipunit.AssertZip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;

public class PackagingMojoTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File buildOutputDir;
    private File buildDir;
    private PackagingMojo mojo;
    private File mavenRepoDir;
    private MavenProject mavenProject;

    @Before
    public void setUp() throws Exception {
        buildOutputDir = temporaryFolder.newFolder("project-output-dir");
        buildDir = temporaryFolder.newFolder("build-dir");
        mavenRepoDir = temporaryFolder.newFolder("maven-repo");

        Build build = new Build();
        build.setOutputDirectory(buildOutputDir.getAbsolutePath());

        mavenProject = new MavenProject();
        mavenProject.setArtifacts(new HashSet<Artifact>());
        mavenProject.setBuild(build);

        mojo = new PackagingMojo();
        mojo.setBuildOutputDir(buildOutputDir);
        mojo.setBuildDir(buildDir);
        mojo.setZipName("plugin");
        mojo.setPluginName("plugin-name");
        mojo.setProject(mavenProject);
    }

    @Test
    public void shouldAddTheRuntimeDependenciesToThePluginBundle() {
        addFileToProjectOutput("1.class");
        addProjectDependency("commons-io:commons-io:1.2");

        runMojo();

        AssertZip.assertEntryExists("lib/commons-io-1.2.jar", pluginBundle());
    }

    @Test
    public void shouldNotDoAnythingWhenNoBuildOutputDirectoryDoesNotExist() {
        mojo.setBuildOutputDir(new File("doesNotExist"));

        runMojo();

        assertFalse(pluginBundle().exists());
    }

    @Test
    public void shouldNotDoAnythingWhenNoBuildDirectoryDoesNotExist() {
        mojo.setBuildDir(new File("doesNotExist"));

        runMojo();

        assertFalse(pluginBundle().exists());
    }

    @Test
    public void shouldCreateAZipFileContainingThePluginJar() {
        addFileToProjectOutput("test.txt");

        runMojo();

        AssertZip.assertEntryExists("lib/plugin-name.jar", pluginBundle());
    }

    @Test
    public void shouldDeleteThePluginJarAfterTheZipWasMade() {
        addFileToProjectOutput("2.class");

        runMojo();

        assertFalse("we should have cleaned up the plugin jar", pluginJar().exists());
    }

    @Test
    public void shouldCreateAJarFileContainingAllTheCompiledAndResourceFiles() {
        mojo.setDeletePluginJar(false);

        addFileToProjectOutput("1.class");
        addFileToProjectOutput("b2s/plugin/2.class");

        runMojo();

        AssertZip.assertEntryExists("1.class", pluginJar());
        AssertZip.assertEntryExists("b2s/plugin/2.class", pluginJar());
    }

    private void addProjectDependency(String definition) {
        String[] parts = definition.split(":");
        File file = new File(mavenRepoDir, parts[1] + "-" + parts[2] + ".jar");
        DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler();
        artifactHandler.setAddedToClasspath(true);
        DefaultArtifact artifact = new DefaultArtifact(
                parts[0],
                parts[1],
                parts[2],
                "compile",
                "jar",
                "main",
                artifactHandler
        );
        artifact.setFile(file);
        write(file, definition);
        mavenProject.getArtifacts().add(artifact);
    }

    private File pluginJar() {
        return new File(buildDir, "plugin-name.jar");
    }

    private File pluginBundle() {
        return new File(buildDir, "plugin.zip");
    }

    private void addFileToProjectOutput(String filePath) {
        File file = new File(buildOutputDir, filePath);
        if (filePath.contains("/")) {
            File dir = new File(buildOutputDir, filePath.substring(0, filePath.lastIndexOf("/")));
            dir.mkdirs();
            file = new File(dir, filePath.substring(filePath.lastIndexOf("/")));
        }
        write(file, filePath);
    }

    private void write(File file, String filePath) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(filePath.getBytes());
            output.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(output);
        }
    }

    private void close(FileOutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {

            }
        }
    }

    private void runMojo() {
        try {
            mojo.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
