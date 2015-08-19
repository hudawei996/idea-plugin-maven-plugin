/**
 * Copyright to the original author or authors.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.github.born2snipe.maven.plugin.idea.sandbox;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

public abstract class BaseSandboxMojo extends AbstractMojo {
    @Parameter(required = true, readonly = true, property = "project")
    protected MavenProject project;
    @Parameter(required = true, defaultValue = "14")
    private String ideaVersion;
    @Parameter
    private File ideaDirectory;
    private IdeaPluginSandboxLocator sandboxLocator = new IdeaPluginSandboxLocator();

    protected File projectSandboxDirectory() {
        return new File(pluginsSandboxDirectory(), project.getName());
    }

    private File pluginsSandboxDirectory() {
        return sandboxLocator.locate(ideaDirectory, ideaVersion);
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setIdeaVersion(String ideaVersion) {
        this.ideaVersion = ideaVersion;
    }

    public void setIdeaDirectory(File ideaDirectory) {
        this.ideaDirectory = ideaDirectory;
    }
}
