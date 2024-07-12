package io.jenkins.plugins.sample;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

public class AndroidEmulatorRunTestReport extends Recorder implements SimpleBuildStep {

    private String gradleCmd;
    private String archiveSourceRoot;
    private String archiveDestinationRoot;

    private AndroidTestReportsAction androidTestReportsAction;

    public String getGradleCmd() {
        return gradleCmd;
    }

    public void setGradleCmd(String gradleCmd) {
        this.gradleCmd = gradleCmd;
    }

    public String getArchiveSourceRoot() {
        return archiveSourceRoot;
    }

    public void setArchiveSourceRoot(String archiveSourceRoot) {
        this.archiveSourceRoot = archiveSourceRoot;
    }

    public String getArchiveDestinationRoot() {
        return archiveDestinationRoot;
    }

    public void setArchiveDestinationRoot(String archiveDestinationRoot) {
        this.archiveDestinationRoot = archiveDestinationRoot;
    }

    public AndroidTestReportsAction getAndroidTestReportsAction() {
        return androidTestReportsAction;
    }

    public void setAndroidTestReportsAction(AndroidTestReportsAction androidTestReportsAction) {
        this.androidTestReportsAction = androidTestReportsAction;
    }

    @DataBoundConstructor
    public AndroidEmulatorRunTestReport(String gradleCmd, String archiveRoot, String archiveDestinationRoot) {
        this.gradleCmd = gradleCmd;
        this.archiveSourceRoot = archiveRoot;
        this.archiveDestinationRoot = archiveDestinationRoot;
        this.androidTestReportsAction = new AndroidTestReportsAction(this.archiveSourceRoot);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        build.addAction(androidTestReportsAction);
        return true;
    }

    @Override
    public void perform(Run<?, ?> run, EnvVars env, TaskListener listener) throws InterruptedException, IOException {
        run.addAction(androidTestReportsAction);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Run Android UI Tests";
        }

        public void doCheckName(@QueryParameter String value) throws IOException, ServletException {}

        // private String gradleCmd;
        // private String archiveSourceRoot;
        // private String archiveDestinationRoot;
        // 通过定义 doCheck+ "属性名称" 方法去校验输入的参数

        public void doCheckGradleCmd(@QueryParameter String value) throws ServletException {}

        public void doCheckArchiveSourceRoot(@QueryParameter String value) throws ServletException {
            if (!(new File(value)).exists()) {
                // FormValidation
            }
        }

        public void doCheckArchiveDestinationRoot(@QueryParameter String value) throws ServletException {}
    }
}
