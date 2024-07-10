package io.jenkins.plugins.sample;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import javax.servlet.ServletException;

import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class AndroidEmulatorRunTestWrapper extends SimpleBuildWrapper {

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

    public AndroidEmulatorRunTestWrapper() {}

    @DataBoundConstructor
    public AndroidEmulatorRunTestWrapper(String gradleCmd, String archiveRoot, String archiveDestinationRoot) {
        this.gradleCmd = gradleCmd;
        this.archiveSourceRoot = archiveRoot;
        this.archiveDestinationRoot = archiveDestinationRoot;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {

    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        public String getDisplayName() {
            return "Run Android Emulator UI Tests";
        }

        public void doCheckName(@QueryParameter String value) throws IOException, ServletException {}

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return false;
        }
    }
}
