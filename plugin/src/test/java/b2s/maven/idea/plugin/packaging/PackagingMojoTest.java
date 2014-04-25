package b2s.maven.idea.plugin.packaging;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import zipunit.AssertZip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertFalse;

public class PackagingMojoTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File buildOutputDir;
    private File buildDir;
    private PackagingMojo mojo;

    @Before
    public void setUp() throws Exception {
        buildOutputDir = temporaryFolder.newFolder("project-output-dir");
        buildDir = temporaryFolder.newFolder("build-dir");

        mojo = new PackagingMojo();
        mojo.setBuildOutputDir(buildOutputDir);
        mojo.setBuildDir(buildDir);
        mojo.setZipName("plugin");
        mojo.setPluginName("plugin-name");
        mojo.setProject(new MavenProject());
    }

    @Test
    public void shouldNotDoAnythingWhenNoBuildOutputDirectoryDoesNotExist() {
        mojo.setBuildOutputDir(new File("doesNotExist"));

        runMojo();

        assertFalse(pluginBundle().exists());
    }

    @Test
    public void shouldNotDoAnythingWhenNoBuildDirectoryDoesNotExist() {
        mojo.setBuildDir(new File("doesNotExist"));

        runMojo();

        assertFalse(pluginBundle().exists());
    }

    @Test
    public void shouldCreateAZipFileContainingThePluginJar() {
        addFileToProjectOutput("test.txt");

        runMojo();

        AssertZip.assertEntryExists("lib/plugin-name.jar", pluginBundle());
    }

    @Test
    public void shouldDeleteThePluginJarAfterTheZipWasMade() {
        addFileToProjectOutput("2.class");

        runMojo();

        assertFalse("we should have cleaned up the plugin jar", pluginJar().exists());
    }

    @Test
    public void shouldCreateAJarFileContainingAllTheCompiledAndResourceFiles() {
        mojo.setDeletePluginJar(false);

        addFileToProjectOutput("1.class");
        addFileToProjectOutput("b2s/plugin/2.class");

        runMojo();

        AssertZip.assertEntryExists("1.class", pluginJar());
        AssertZip.assertEntryExists("b2s/plugin/2.class", pluginJar());
    }

    private File pluginJar() {
        return new File(buildDir, "plugin-name.jar");
    }

    private File pluginBundle() {
        return new File(buildDir, "plugin.zip");
    }

    private void addFileToProjectOutput(String filePath) {
        File file = new File(buildOutputDir, filePath);
        if (filePath.contains("/")) {
            File dir = new File(buildOutputDir, filePath.substring(0, filePath.lastIndexOf("/")));
            dir.mkdirs();
            file = new File(dir, filePath.substring(filePath.lastIndexOf("/")));
        }
        write(file, filePath);
    }

    private void write(File file, String filePath) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(filePath.getBytes());
            output.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(output);
        }
    }

    private void close(FileOutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {

            }
        }
    }

    private void runMojo() {
        try {
            mojo.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
