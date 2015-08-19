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
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdeaPluginSandboxLocatorTest {
    private static final String IDE_VERSION = "12";
    @InjectMocks
    private IdeaPluginSandboxLocator locator;
    @Mock
    private IdeaCacheLocator cacheLocator;
    private File ideaRootDirectory;
    private File cacheDir;
    private File pluginSandboxDir;

    @Before
    public void setUp() throws Exception {
        ideaRootDirectory = new File("idea-root");
        cacheDir = new File("cache-dir");
        pluginSandboxDir = new File(cacheDir, "plugins-sandbox/plugins");
    }

    @Test
    public void shouldDetermineTheSandboxDirectory() {
        when(cacheLocator.locateFrom(ideaRootDirectory, IDE_VERSION)).thenReturn(cacheDir);

        assertEquals(pluginSandboxDir, locator.locate(ideaRootDirectory, IDE_VERSION));
    }

}