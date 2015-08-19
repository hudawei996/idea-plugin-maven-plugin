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
package com.github.born2snipe.maven.plugin.idea;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;

public abstract class BaseMojoTestCase {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected MavenProject mavenProject;
    protected File buildOutputDir;
    protected File buildDir;
    protected File mavenRepoDir;

    @Before
    public void baseSetUp() throws Exception {
        buildOutputDir = temporaryFolder.newFolder("project-output-dir");
        buildDir = temporaryFolder.newFolder("build-dir");
        mavenRepoDir = temporaryFolder.newFolder("maven-repo");

        Build build = new Build();
        build.setOutputDirectory(buildOutputDir.getAbsolutePath());

        mavenProject = new MavenProject();
        mavenProject.setName("plugin-name");
        mavenProject.setArtifacts(new HashSet<>());
        mavenProject.setBuild(build);
    }

    protected File pluginJar() {
        return new File(buildDir, "plugin-name.jar");
    }

    protected void addFileToProjectOutput(String filePath) {
        File file = new File(buildOutputDir, filePath);
        if (filePath.contains("/")) {
            File dir = new File(buildOutputDir, filePath.substring(0, filePath.lastIndexOf("/")));
            dir.mkdirs();
            file = new File(dir, filePath.substring(filePath.lastIndexOf("/")));
        }
        writeFile(file, filePath);
    }

    protected void addProjectDependency(String definition) {
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
        writeFile(file, definition);
        mavenProject.getArtifacts().add(artifact);
    }

    protected void writeFile(File file, String contents) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(contents.getBytes());
            output.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    protected void run(Mojo mojo) {
        try {
            mojo.execute();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
