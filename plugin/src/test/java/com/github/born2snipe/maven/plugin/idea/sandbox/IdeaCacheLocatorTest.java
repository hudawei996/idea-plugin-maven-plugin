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

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.github.born2snipe.maven.plugin.idea.sandbox.Env.*;
import static org.junit.Assert.assertEquals;

public class IdeaCacheLocatorTest {
    private static final String ORIGINAL_OS_NAME = System.getProperty("os.name");
    private static final String ORIGINAL_USER_HOME = System.getProperty("user.home");
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private IdeaCacheLocator locator;
    private File root;
    private File cache;
    private File macCacheDir;
    private File windowsAndLinuxCacheDir;

    @AfterClass
    public static void reset() {
        System.setProperty("os.name", ORIGINAL_OS_NAME);
        System.setProperty("user.home", ORIGINAL_USER_HOME);
    }

    @Before
    public void setUp() throws Exception {
        locator = new IdeaCacheLocator();

        root = temporaryFolder.newFolder("root");
        cache = temporaryFolder.newFolder("cache");
        File userHome = temporaryFolder.newFile("user-home");

        macCacheDir = new File(userHome, "Library/Caches/IntelliJIdea13");
        windowsAndLinuxCacheDir = new File(userHome, "IntelliJIdea13");

        System.setProperty("user.home", userHome.getAbsolutePath());
        initializeIdeaProperties(false);
    }

    @Test
    public void shouldUseTheOsDefaultLocationWhenNoOverriddenPropertyIsProvided_Windows() {
        osIs(WINDOWS);

        assertEquals(windowsAndLinuxCacheDir, locator.locateFrom(root, "13"));
    }

    @Test
    public void shouldUseTheOsDefaultLocationWhenNoOverriddenPropertyIsProvided_Linux() {
        osIs(LINUX);

        assertEquals(windowsAndLinuxCacheDir, locator.locateFrom(root, "13"));
    }

    @Test
    public void shouldUseTheOsDefaultLocationWhenNoOverriddenPropertyIsProvided_Mac() {
        osIs(MAC);

        assertEquals(macCacheDir, locator.locateFrom(root, "13"));
    }

    @Test
    public void shouldReadTheConfigurationOfTheIdeToDetermineWhereTheCacheLivesIfAvailable() {
        initializeIdeaProperties(true);

        assertEquals(cache, locator.locateFrom(root, "13"));
    }

    @Test
    public void shouldUseTheDefaultsIfTheIDEAPathIsNotProvided() {
        osIs(LINUX);

        assertEquals(windowsAndLinuxCacheDir, locator.locateFrom(null, "13"));
    }

    private void osIs(Env env) {
        System.setProperty("os.name", env.name());
    }

    private void initializeIdeaProperties(boolean hasOverriddenPath) {
        File bin = new File(root, "bin");
        bin.mkdirs();

        String contents = "";
        File properties = new File(bin, "idea.properties");
        if (hasOverriddenPath) {
            contents = "idea.system.path=" + cache.getAbsolutePath();
        }

        try (OutputStream output = new FileOutputStream(properties)) {
            IOUtils.write(contents, output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}