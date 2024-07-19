package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class AndroidEmulatorRunTestReport extends Builder implements SimpleBuildStep {

    private String gradleCmd;
    private String archiveSourceRoot;
    private String archiveDestinationRoot;

    public String getGradleCmd() {
        return gradleCmd;
    }

    @DataBoundSetter
    public void setGradleCmd(String gradleCmd) {
        this.gradleCmd = gradleCmd;
    }

    public String getArchiveSourceRoot() {
        return archiveSourceRoot;
    }

    @DataBoundSetter
    public void setArchiveSourceRoot(String archiveSourceRoot) {
        this.archiveSourceRoot = archiveSourceRoot;
    }

    public String getArchiveDestinationRoot() {
        return archiveDestinationRoot;
    }

    @DataBoundSetter
    public void setArchiveDestinationRoot(String archiveDestinationRoot) {
        this.archiveDestinationRoot = archiveDestinationRoot;
    }

    @DataBoundConstructor
    public AndroidEmulatorRunTestReport(String gradleCmd, String archiveRoot, String archiveDestinationRoot) {
        this.gradleCmd = gradleCmd;
        this.archiveSourceRoot = archiveRoot;
        this.archiveDestinationRoot = archiveDestinationRoot;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {
        run.addAction(new AndroidTestReportsAction(run, this.archiveSourceRoot));
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            super(AndroidEmulatorRunTestReport.class);
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            save();
            return true;
        }

        @NonNull
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
