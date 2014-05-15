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
package com.github.born2snipe.maven.plugin.idea.packaging;

import com.github.born2snipe.maven.plugin.idea.BaseMojoTestCase;
import org.junit.Before;
import org.junit.Test;
import zipunit.AssertZip;

import java.io.File;

import static org.junit.Assert.assertFalse;

public class PackagingMojoTest extends BaseMojoTestCase {
    private PackagingMojo mojo;

    @Before
    public void setUp() throws Exception {
        mojo = new PackagingMojo();
        mojo.setBuildOutputDir(buildOutputDir);
        mojo.setBuildDir(buildDir);
        mojo.setZipName("plugin");
        mojo.setPluginName("plugin-name");
        mojo.setProject(mavenProject);
    }

    @Test
    public void shouldAlwaysAddAManifestFileToThePluginsJar() {
        mojo.setDeletePluginJar(false);
        addFileToProjectOutput("1.class");

        run(mojo);

        String expectedContents = "Manifest-Version: 1.0\n" +
                "Created-By: IntelliJ IDEA\n";
        AssertZip.assertEntry("META-INF/MANIFEST.MF", expectedContents, pluginJar());
    }

    @Test
    public void shouldAddTheRuntimeDependenciesToThePluginBundle() {
        addFileToProjectOutput("1.class");
        addProjectDependency("commons-io:commons-io:1.2");

        run(mojo);

        AssertZip.assertEntryExists("plugin-name/lib/commons-io-1.2.jar", pluginBundle());
    }

    @Test
    public void shouldNotDoAnythingWhenNoBuildOutputDirectoryDoesNotExist() {
        mojo.setBuildOutputDir(new File("doesNotExist"));

        run(mojo);

        assertFalse(pluginBundle().exists());
    }

    @Test
    public void shouldNotDoAnythingWhenNoBuildDirectoryDoesNotExist() {
        mojo.setBuildDir(new File("doesNotExist"));

        run(mojo);

        assertFalse(pluginBundle().exists());
    }

    @Test
    public void shouldCreateAZipFileContainingThePluginJar() {
        addFileToProjectOutput("test.txt");

        run(mojo);

        AssertZip.assertEntryExists("plugin-name/lib/plugin-name.jar", pluginBundle());
    }

    @Test
    public void shouldDeleteThePluginJarAfterTheZipWasMade() {
        addFileToProjectOutput("2.class");

        run(mojo);

        assertFalse("we should have cleaned up the plugin jar", pluginJar().exists());
    }

    @Test
    public void shouldCreateAJarFileContainingAllTheCompiledAndResourceFiles() {
        mojo.setDeletePluginJar(false);

        addFileToProjectOutput("1.class");
        addFileToProjectOutput("b2s/plugin/2.class");

        run(mojo);

        AssertZip.assertEntryExists("1.class", pluginJar());
        AssertZip.assertEntryExists("b2s/plugin/2.class", pluginJar());
    }

    private File pluginBundle() {
        return new File(buildDir, "plugin.zip");
    }

}
