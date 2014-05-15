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
package com.github.born2snipe.maven.plugin.idea.sandbox;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

@Mojo(name = "copy-plugin-to-sandbox", requiresProject = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CopyPluginToPluginSandboxMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "project")
    private MavenProject project;
    @Parameter(defaultValue = "${user.home}/Library/Caches/IntelliJIdea13")
    private File ideaCacheDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Sandbox directory: " + projectSandboxDirectory());
        long start = System.currentTimeMillis();
        copyProjectDependenciesToSandbox();
        copyPluginXmlToSandbox();
        copyClassesToSandbox();
        getLog().info("Copied everything to the sandbox in " + (System.currentTimeMillis() - start) + " millis");
    }

    private void copyClassesToSandbox() {
        getLog().info("Copying classes to sandbox");
        File classesSandbox = new File(projectSandboxDirectory(), "classes");
        classesSandbox.mkdirs();

        File outputDirectory = new File(project.getBuild().getOutputDirectory());
        Iterator<File> iterator = FileUtils.iterateFiles(outputDirectory, null, true);
        while (iterator.hasNext()) {
            File file = iterator.next();
            if ("plugin.xml".equals(file.getName())) {
                continue;
            }

            String newPath = file.getAbsolutePath().replace(outputDirectory.getAbsolutePath(), "");
            File classFileDestination = new File(classesSandbox, newPath);
            copyFileTo(file, classFileDestination.getParentFile());
        }
    }

    private void copyPluginXmlToSandbox() throws MojoExecutionException {
        getLog().info("Copying plugin.xml to sandbox");
        File pluginXml = new File(project.getBuild().getOutputDirectory(), "META-INF/plugin.xml");
        if (pluginXml.exists()) {
            copyFileTo(pluginXml, new File(projectSandboxDirectory(), "META-INF"));
        } else {
            getLog().error("Could not locate [plugin.xml]. Was looking here: " + pluginXml);
            throw new MojoExecutionException("No plugin.xml file was found");
        }
    }

    private void copyProjectDependenciesToSandbox() throws MojoExecutionException {
        getLog().info("Copying dependencies to sandbox");
        File directory = dependencyDirectory();
        try {
            for (String filePath : project.getRuntimeClasspathElements()) {
                if (isOutputDirectory(filePath)) {
                    continue;
                }
                copyFileTo(new File(filePath), directory);
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("A problem occurred when attempting to copy the dependencies to the sandbox", e);
        }
    }

    private boolean isOutputDirectory(String filePath) {
        return project.getBuild().getOutputDirectory().equals(filePath);
    }

    private void copyFileTo(File sourceFile, File directory) {
        InputStream input = null;
        OutputStream output = null;
        try {
            directory.mkdirs();
            input = new FileInputStream(sourceFile);
            output = new FileOutputStream(new File(directory, sourceFile.getName()));
            IOUtils.copy(input, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    private File dependencyDirectory() {
        File directory = new File(projectSandboxDirectory(), "lib");
        directory.mkdirs();
        return directory;
    }

    private File projectSandboxDirectory() {
        return new File(pluginsSandboxDirectory(), project.getName());
    }

    private File pluginsSandboxDirectory() {
        return new File(ideaCacheDirectory, "plugins-sandbox/plugins");
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setIdeaCacheDirectory(File ideaCacheDirectory) {
        this.ideaCacheDirectory = ideaCacheDirectory;
    }
}
