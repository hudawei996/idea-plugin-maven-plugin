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
package output;

import org.junit.Before;
import org.junit.Test;
import zipunit.AssertZip;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

public class OutputIT {
    private File outputDir;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("test.properties"));
        outputDir = new File(properties.getProperty("outputDir"));
    }

    @Test
    public void shouldEnsureThePluginBundleIsBuiltWithTheExpectedContents() {
        File zip = pluginBundle();

        AssertZip.assertEntryExists("idea-plugin-integration/lib/idea-plugin-integration.jar", zip);
        AssertZip.assertEntryExists("idea-plugin-integration/lib/junit-4.11.jar", zip);
    }

    private File pluginBundle() {
        File[] zips = outputDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        });

        return zips[0];
    }
}
