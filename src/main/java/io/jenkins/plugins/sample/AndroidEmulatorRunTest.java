package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class AndroidEmulatorRunTest extends Builder {

    private String gradleCmd;
    private String archiveSourceRoot;
    private String archiveDestinationRoot;

    public String getGradleCmd() {
        return gradleCmd;
    }

    public String getArchiveSourceRoot() {
        return archiveSourceRoot;
    }

    public String getArchiveDestinationRoot() {
        return archiveDestinationRoot;
    }

    public AndroidEmulatorRunTest() {}

    @DataBoundConstructor
    public AndroidEmulatorRunTest(String gradleCmd, String archiveRoot, String archiveDestinationRoot) {
        this.gradleCmd = gradleCmd;
        this.archiveSourceRoot = archiveRoot;
        this.archiveDestinationRoot = archiveDestinationRoot;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        // ProcessBuilder builder = new ProcessBuilder();
        // builder.command("bash", "-c", "./" + gradleCmd);
        // Process process = builder.start();
        // int exitCode = process.waitFor();
        // builder.command("bash", "-c", "cp " + archiveSourceRoot + " " + archiveDestinationRoot );
        // Process process2 = builder.start();
        // process2.waitFor();
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Run Android Emulator and UI Tests";
        }

        public void doCheckName(@QueryParameter String value) throws IOException, ServletException {}
    }
}
