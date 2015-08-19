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

import com.github.born2snipe.maven.plugin.idea.BaseMojoTestCase;
import org.junit.AfterClass;
import org.junit.Before;

import java.io.File;

public abstract class BaseSandboxMojoTestCase extends BaseMojoTestCase {
    protected static final String IDEA_VERSION = "13";
    private static final String ORIGINAL_USER_HOME = System.getProperty("user.home");
    protected File pluginsSandboxDir;
    protected File intellijCache;
    protected File ideaRootDir;
    private File ideaProperties;

    @AfterClass
    public static void reset() {
        System.setProperty("user.home", ORIGINAL_USER_HOME);
    }

    @Before
    public void baseSandboxSetup() throws Exception {
        File userHome = temporaryFolder.newFolder("user-home");
        System.setProperty("user.home", userHome.getAbsolutePath());

        ideaRootDir = temporaryFolder.newFolder("idea-root");
        setupIdeaProperties();

        intellijCache = Env.current().getCacheDirectoryFor(IDEA_VERSION);
        pluginsSandboxDir = new File(intellijCache, "plugins-sandbox/plugins");
        pluginsSandboxDir.mkdirs();
    }

    protected File projectSandboxDir() {
        return new File(pluginsSandboxDir, mavenProject.getName());
    }

    private void setupIdeaProperties() {
        File binDirectory = new File(ideaRootDir, "bin");
        binDirectory.mkdirs();
        ideaProperties = new File(binDirectory, "idea.properties");
        writeFile(ideaProperties, "");
    }

}
