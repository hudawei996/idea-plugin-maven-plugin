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

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import zipunit.ZipBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

@Mojo(name = "package-plugin",
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        defaultPhase = LifecyclePhase.PACKAGE)
public class PackagingMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File buildOutputDir;
    @Parameter(defaultValue = "${project.build.finalName}")
    private String zipName;
    @Parameter(defaultValue = "${project.name}")
    private String pluginName;
    @Parameter(defaultValue = "${project.build.directory}")
    private File buildDir;
    @Parameter(required = true, readonly = true, property = "project")
    private MavenProject project;
    private boolean deletePluginJar = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (doesBuildDirectoryNotExist()) {
            return;
        }

        File pluginJar = buildPluginJarFile();

        buildPluginBundle(pluginJar);

        if (deletePluginJar) {
            pluginJar.delete();
        }
    }

    private boolean doesBuildDirectoryNotExist() {
        return !buildDir.exists() || !buildOutputDir.exists();
    }

    private void buildPluginBundle(File pluginJar) {
        ZipBuilder zipBuilder = new ZipBuilder(buildDir);
        addToBundle(pluginJar, zipBuilder);
        addDependenciesToBundle(zipBuilder);
        zipBuilder.build(zipName + ".zip");
    }

    private void addDependenciesToBundle(ZipBuilder zipBuilder) {
        try {
            for (String classPathElement : project.getRuntimeClasspathElements()) {
                if (isOutputDirectory(classPathElement)) {
                    continue;
                }

                addToBundle(new File(classPathElement), zipBuilder);
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isOutputDirectory(String path) {
        return buildOutputDir.getAbsolutePath().equals(path);
    }

    private void addToBundle(File jarFile, ZipBuilder zipBuilder) {
        zipBuilder.withEntry("lib/" + jarFile.getName(), inputStreamFor(jarFile));
    }

    private File buildPluginJarFile() {
        ZipBuilder zipBuilder = new ZipBuilder(buildDir);

        Iterator<File> iterator = FileUtils.iterateFiles(buildOutputDir, null, true);
        while (iterator.hasNext()) {
            File file = iterator.next();
            String entry = convertToEntryPath(file);
            zipBuilder.withEntry(entry, inputStreamFor(file));
        }

        return zipBuilder.build(pluginName + ".jar");
    }

    private String convertToEntryPath(File file) {
        String wholePath = file.getAbsolutePath();
        String outputPath = buildOutputDir.getAbsolutePath();
        return wholePath.replace(outputPath + File.separator, "");
    }

    private BufferedInputStream inputStreamFor(File file) {
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBuildOutputDir(File buildOutputDir) {
        this.buildOutputDir = buildOutputDir;
    }

    public void setZipName(String zipName) {
        this.zipName = zipName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public void setDeletePluginJar(boolean deletePluginJar) {
        this.deletePluginJar = deletePluginJar;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }
}
