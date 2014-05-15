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

import static org.junit.Assert.assertEquals;

public class DeletePluginFromSandboxMojoTest extends BaseSandboxMojoTestCase {
    private DeletePluginFromSandboxMojo mojo;

    @Before
    public void setUp() throws Exception {
        mojo = new DeletePluginFromSandboxMojo();
        mojo.setProject(mavenProject);
        mojo.setIdeaCacheDirectory(intellijCache);
    }

    @Test
    public void shouldDeleteOldPluginContentsFromThePluginSandbox() {
        whenOurPluginIsAlreadyInTheSandbox();

        run(mojo);

        assertPluginIsNotInSandbox();
    }

    private void assertPluginIsNotInSandbox() {
        File[] files = pluginsSandboxDir.listFiles();
        assertEquals("we expected no plugins to be found: found=" + Arrays.toString(files), 0, files.length);
    }

    private void whenOurPluginIsAlreadyInTheSandbox() {
        whenAFileExistsIn(new File(projectSandboxDir(), "lib"));
    }

    private void whenAFileExistsIn(File directory) {
        directory.mkdirs();
        writeFile(new File(directory, "blah.txt"), "blah");
    }
}
