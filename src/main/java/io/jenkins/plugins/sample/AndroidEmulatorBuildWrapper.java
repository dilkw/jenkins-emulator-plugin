package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;


import hudson.util.FormValidation;
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

    private final String buildTools;
    private final String androidOSVersion;
    private final String density;
    private final String resolution;
    private String SDKRoot;
    private String emulatorName;
    private String deviceDefinition;
    private String deviceLocale;
    private String SDCardSize;
    private String targetABI;
    private int adbTimeout;
    private boolean enableOptions;

    private List<HardwareProperty> hardwareProperties = new ArrayList<>();

    private boolean configToolsEnable;
    private String sdkManagerRoot;
    private String avdManagerRoot;
    private String adbToolRoot;
    private String emulatorToolRoot;
    private DescriptorImpl descriptor;

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
    public String getDeviceDefinition() {
        return deviceDefinition;
    }
    public String getSDCardSize() {
        return SDCardSize;
    }
    public String getTargetABI() {
        return targetABI;
    }
    public boolean isConfigToolsEnable() {
        return configToolsEnable;
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

    @DataBoundSetter
    public void setAdbTimeout(int adbTimeout) {
        this.adbTimeout = adbTimeout;
    }

    @DataBoundSetter
    public void setDescriptor(DescriptorImpl descriptor) {
        this.descriptor = descriptor;
    }

    @DataBoundSetter
    public void setConfigToolsEnable(boolean configToolsEnable) {
        this.configToolsEnable = configToolsEnable;
    }

    @DataBoundSetter
    public void setSdkManagerRoot(String sdkManagerRoot) {
        this.sdkManagerRoot = sdkManagerRoot;
    }

    @DataBoundSetter
    public void setAvdManagerRoot(String avdManagerRoot) {
        this.avdManagerRoot = avdManagerRoot;
    }

    @DataBoundSetter
    public void setAdbToolRoot(String adbToolRoot) {
        this.adbToolRoot = adbToolRoot;
    }

    @DataBoundSetter
    public void setEmulatorToolRoot(String emulatorToolRoot) {
        this.emulatorToolRoot = emulatorToolRoot;
    }

    @DataBoundSetter
    public void setEmulatorName(String emulatorName) {
        this.emulatorName = emulatorName;
    }

    @DataBoundSetter
    public void setSDKRoot(String SDKRoot) {
        this.SDKRoot = SDKRoot;
    }

    @DataBoundSetter
    public void setDeviceDefinition(String deviceDefinition) {
        this.deviceDefinition = deviceDefinition;
    }

    @DataBoundSetter
    public void setSDCardSize(String SDCardSize) {
        this.SDCardSize = SDCardSize;
    }

    @DataBoundSetter
    public void setDeviceLocale(String deviceLocale) {
        this.deviceLocale = deviceLocale;
    }

    @DataBoundSetter
    public void setTargetABI(String targetABI) {
        this.targetABI = targetABI;
    }

    @DataBoundSetter
    public void setEnableOptions(boolean enableOptions) {
        this.enableOptions = enableOptions;
    }

    @DataBoundConstructor
    public AndroidEmulatorBuildWrapper(@CheckForNull String buildTools, String androidOSVersion, String density, String resolution) {
        this.buildTools = buildTools;
        this.androidOSVersion = androidOSVersion;
        this.density = density;
        this.resolution = resolution;
    }


    @Override
    public void setUp(Context context, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {

        if (descriptor == null) {
            descriptor = Jenkins.get().getDescriptorByType(DescriptorImpl.class);
        }

        System.out.println("initialEnvironment" + initialEnvironment.toString());
        System.out.println("androidHome:" + SDKRoot);
        initialEnvironment.put(Constants.ENV_VAR_ANDROID_SDK_ROOT, SDKRoot);
        buildEnvVars(workspace, initialEnvironment);
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

        public DescriptorImpl() {
            super(AndroidEmulatorBuildWrapper.class);
            load();
        }

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

    public void buildEnvVars(@NonNull FilePath homeLocation, @CheckForNull EnvVars env) throws IOException, InterruptedException {
        if (env == null) {
            env = new EnvVars();
        }
        env.put(Constants.ENV_ANDROID_SDK_HOME, homeLocation.getRemote());

        FilePath emulatorLocation = homeLocation.child(Constants.ANDROID_CACHE);
        env.put(Constants.ENV_ANDROID_EMULATOR_HOME, emulatorLocation.getRemote());

        FilePath avdPath = emulatorLocation.child("avd");
        avdPath.mkdirs(); // ensure that this folder exists
        env.put(Constants.ENV_ANDROID_AVD_HOME, avdPath.getRemote());
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
