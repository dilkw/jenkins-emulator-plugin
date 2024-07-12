package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;


import hudson.util.FormValidation;
import io.jenkins.plugins.sample.cmd.ADBManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.ChristelleCLICommand;
import io.jenkins.plugins.sample.cmd.SDKManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.model.EmulatorConfig;
import io.jenkins.plugins.sample.cmd.model.HardwareProperty;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AndroidEmulatorBuildWrapper extends SimpleBuildWrapper {

    private final boolean enableOptions;
    private final String SDKRoot;
    private final String buildTools;
    private final String emulatorName;
    private final String androidOSVersion;
    private final String density;
    private final String resolution;
    private final String deviceDefinition;
    private final String deviceLocale;
    private final String SDCardSize;
    private final String targetABI;
    private List<HardwareProperty> hardwareProperties = new ArrayList<>();

    private int adbTimeout;

    private DescriptorImpl descriptor;

    private boolean configTollsEnable;
    private String sdkManagerRoot;
    private String avdManagerRoot;
    private String adbToolRoot;
    private String emulatorToolRoot;

    public boolean isEnableOptions() {
        return enableOptions;
    }
    public String getSDKRoot() {
        return SDKRoot;
    }
    public String getBuildTools() {
        return buildTools;
    }
    public String getEmulatorName() {
        return emulatorName;
    }
    public String getAndroidOSVersion() {
        return androidOSVersion;
    }
    public String getDensity() {
        return density;
    }
    public String getResolution() {
        return resolution;
    }
    public String getDeviceLocale() {
        return deviceLocale;
    }
    public String getSDCardSize() {
        return SDCardSize;
    }
    public String getTargetABI() {
        return targetABI;
    }

    public boolean isConfigTollsEnable() {
        return configTollsEnable;
    }
    public String getSdkManagerRoot() {
        return sdkManagerRoot;
    }
    public String getAvdManagerRoot() {
        return avdManagerRoot;
    }
    public String getAdbToolRoot() {
        return adbToolRoot;
    }
    public String getEmulatorToolRoot() {
        return emulatorToolRoot;
    }

    @DataBoundConstructor
    public AndroidEmulatorBuildWrapper(
            boolean enableOptions,
            String SDKRoot,
            String buildTools,
            String emulatorName,
            String androidOSVersion,
            String density,
            String resolution, String deviceDefinition,
            String deviceLocale,
            String SDCardSize,
            String targetABI) {
        this.enableOptions = enableOptions;
        this.SDKRoot = SDKRoot;
        this.buildTools = buildTools;
        this.emulatorName = emulatorName;
        this.androidOSVersion = androidOSVersion;
        this.density = density;
        this.resolution = resolution;
        this.deviceDefinition = deviceDefinition;
        this.deviceLocale = deviceLocale;
        this.SDCardSize = SDCardSize;
        this.targetABI = targetABI;
    }


    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {

        if (descriptor == null) {
            descriptor = Jenkins.get().getDescriptorByType(DescriptorImpl.class);
        }

        try {

            final EnvVars env = initialEnvironment.overrideAll(context.getEnv());
            EmulatorConfig config = new EmulatorConfig();
            config.setAvdAndroidAPI(Util.replaceMacro(androidOSVersion, env));
            config.setDensity(Util.replaceMacro(density, env));
            config.setResolution(Util.replaceMacro(resolution, env));
            config.setEmulatorName(Util.replaceMacro(emulatorName, env));
            config.setDeviceLocale(Util.replaceMacro(deviceLocale, env));
            config.setDeviceDefinition(Util.replaceMacro(deviceDefinition, env));
            config.setSdkCardSize(Util.replaceMacro(SDCardSize, env));
            config.setTargetABI(Util.replaceMacro(targetABI, env));
            config.setHardwareProperties(hardwareProperties.stream() //
                    .map(p -> new HardwareProperty(Util.replaceMacro(p.getKey(), env), Util.replaceMacro(p.getValue(), env))) //
                    .collect(Collectors.toList()));
            config.setAdbConnectionTimeout(adbTimeout * 1000);
            config.setReportConsolePort(55000);

            // validate input
            Collection<EmulatorConfig.ValidationError> errors = config.validate();
            if (!errors.isEmpty()) {
                throw new AbortException(StringUtils.join(errors, "\n"));
            }

            EmulatorRunner emulatorRunner = new EmulatorRunner(config);
            emulatorRunner.run(workspace, listener, env);

            System.out.println("initialEnvironment: " + initialEnvironment.toString());
            String sdkRoot = initialEnvironment.get(Constants.ANDROID_SDK_ROOT);
            ChristelleCLICommand<Void> cliCommand = SDKManagerCLIBuilder.withSDKRoot(sdkRoot)
                    .addEnvironment(initialEnvironment)
                    .buildCommand();
            cliCommand.run();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DataBoundSetter
    public void setHardwareProperties(List<HardwareProperty> hardwareProperties) {
        this.hardwareProperties = hardwareProperties;
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        String androidHome;
        boolean shouldInstallSdk;
        boolean shouldKeepInWorkspace;

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Run Android Emulator on build";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            androidHome = json.optString("androidHome");
            shouldInstallSdk = json.optBoolean("shouldInstallSdk", true);
            shouldKeepInWorkspace = json.optBoolean("shouldKeepInWorkspace", false);
            save();
            return true;
        }

        // 检查Android Home 目录是否正确
        public FormValidation doCheckAndroidHome(@QueryParameter String value) throws IOException, InterruptedException {
            FilePath sdkPath = new FilePath(new File(value));
            if (!sdkPath.exists()) {
                return FormValidation.error("AndroidHome does not exist");
            }
            return FormValidation.ok();
        }
    }

    public boolean checkFilePath(String pathString) {
        File file = new File(pathString);
        return file.exists();
    }

    private static class EnvVarsAdapter extends EnvVars {
        private static final long serialVersionUID = 1L;

        private final transient Context context; // NOSONAR

        public EnvVarsAdapter(@NonNull Context context) {
            this.context = context;
        }

        @Override
        public String put(String key, String value) {
            context.env(key, value);
            return null; // old value does not exist, just one binding for key
        }

        @Override
        public void override(String key, String value) {
            put(key, value);
        }
    }
}
