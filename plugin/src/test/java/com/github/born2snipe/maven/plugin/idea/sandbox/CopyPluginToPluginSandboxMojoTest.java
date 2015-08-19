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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CopyPluginToPluginSandboxMojoTest extends BaseSandboxMojoTestCase {
    private CopyPluginToPluginSandboxMojo mojo;

    @Before
    public void setUp() throws Exception {
        mojo = new CopyPluginToPluginSandboxMojo();
        mojo.setProject(mavenProject);
        mojo.setIdeaVersion(IDEA_VERSION);
        mojo.setIdeaDirectory(ideaRootDir);

        addFileToProjectOutput("META-INF/plugin.xml");
    }

    @Test
    public void shouldNotCopyThePluginXmlToTheClassesDirectory() {
        run(mojo);

        File classesDir = new File(projectSandboxDir(), "classes");
        assertFalse(new File(classesDir, "META-INF/plugin.xml").exists());
    }

    @Test
    public void shouldCopyAllTheCompiledClassesToTheSandbox() {
        addFileToProjectOutput("package/Test.class");
        addFileToProjectOutput("package/is/longer/Test.class");

        run(mojo);

        File classesDir1 = new File(projectSandboxDir(), "classes");
        assertTrue("we should have made a classes directory", classesDir1.exists());
        File classFile1 = new File(classesDir1, "package/Test.class");
        assertTrue(classFile1.exists());
        assertFalse("This file should be a file, not a directory", classFile1.isDirectory());
        File classesDir = new File(projectSandboxDir(), "classes");
        assertTrue("we should have made a classes directory", classesDir.exists());
        File classFile = new File(classesDir, "package/is/longer/Test.class");
        assertTrue(classFile.exists());
        assertFalse("This file should be a file, not a directory", classFile.isDirectory());
    }

    @Test(expected = Exception.class)
    public void shouldBlowUpIfNoPluginXmlIsFound() {
        whenNoPluginXmlExists();

        run(mojo);
    }

    @Test
    public void shouldCopyThePluginXmlFileToTheSandbox() {
        run(mojo);

        File metaInfDir = new File(projectSandboxDir(), "META-INF");
        assertTrue("we should have made the META-INF directory", metaInfDir.exists());
        assertEquals(Arrays.asList("plugin.xml"), Arrays.asList(metaInfDir.list()));
    }

    @Test
    public void shouldCopyAllTheDependenciesToTheSandbox() {
        addProjectDependency("commons-io:commons-io:1.2");
        addProjectDependency("commons-lang:commons-lang:7");

        run(mojo);

        assertDependenciesInSandbox("commons-io-1.2.jar", "commons-lang-7.jar");
    }

    private void whenNoPluginXmlExists() {
        new File(buildOutputDir, "META-INF/plugin.xml").delete();
    }

    private void assertDependenciesInSandbox(String... expectedDependencies) {
        File projectSandboxDir = projectSandboxDir();
        File projectDependenciesDir = new File(projectSandboxDir, "lib");
        assertTrue("dependency directory does not exist in sandbox", projectDependenciesDir.exists());
        assertEquals(Arrays.asList(expectedDependencies), Arrays.asList(projectDependenciesDir.list()));
    }

}
