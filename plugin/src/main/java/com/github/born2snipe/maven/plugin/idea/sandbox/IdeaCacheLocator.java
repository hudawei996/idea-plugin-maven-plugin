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

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IdeaCacheLocator {
    public File locateFrom(File ideaRootDirectory, String ideVersion) {
        return determineDirectory(ideaRootDirectory, ideVersion);
    }

    private File determineDirectory(File ideaRootDirectory, String ideVersion) {
        File directory = determineDefaultDirectory(ideVersion);

        if (ideaRootDirectory != null) {
            String overriddenPath = getOverriddenPath(ideaRootDirectory);
            if (StringUtils.isNotBlank(overriddenPath)) {
                directory = new File(overriddenPath);
            }
        }
        return directory;
    }

    private File determineDefaultDirectory(String ideVersion) {
        return Env.current().getCacheDirectoryFor(ideVersion);
    }

    private String getOverriddenPath(File ideaRootDirectory) {
        File ideaProperties = new File(ideaRootDirectory, "bin/idea.properties");
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(ideaProperties)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.getProperty("idea.system.path");
    }
}
