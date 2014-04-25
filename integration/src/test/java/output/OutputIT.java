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

        AssertZip.assertEntryExists("lib/idea-plugin-integration.jar", zip);
        AssertZip.assertEntryExists("lib/junit-4.11.jar", zip);
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
