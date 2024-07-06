package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import java.io.File;
import org.kohsuke.stapler.DataBoundConstructor;

public class EmulaorRunTestBuildWrapper extends BuildWrapper {

    private boolean enableOptions;
    private String SDKRoot;
    private String buildTools;
    private String emulatorName;
    private String androidOSVersion;
    private String density;
    private String resolution;
    private String devideLocale;
    private String SDCardSize;
    private String targetABI;

    public boolean isEnableOptions() {return enableOptions;}

    public String getSDKRoot() {return SDKRoot;}

    public String getBuildTools() {return buildTools;}

    public String getEmulatorName() {return emulatorName;}

    public String getAndroidOSVersion() {return androidOSVersion;}

    public String getDensity() {return density;}

    public String getResolution() {return resolution;}

    public String getDevideLocale() {return devideLocale;}

    public String getSDCardSize() {return SDCardSize;}

    public String getTargetABI() {return targetABI;}

    @DataBoundConstructor
    public EmulaorRunTestBuildWrapper(
        boolean enableOptions,
        String SDKRoot,
        String buildTools,
        String emulatorName,
        String androidOSVersion,
        String density,
        String resolution,
        String devideLocale,
        String SDCardSize,
        String targetABI
    ) {
        this.enableOptions = enableOptions;
        this.SDKRoot = SDKRoot;
        this.buildTools = buildTools;
        this.emulatorName = emulatorName;
        this.androidOSVersion = androidOSVersion;
        this.density = density;
        this.resolution = resolution;
        this.devideLocale = devideLocale;
        this.SDCardSize = SDCardSize;
        this.targetABI = targetABI;
    }

    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Run Android Emulator and UI Tests";
        }

        
    }


    public boolean checkFilePath(String pathString) {
        File file = new File(pathString);
        return file.exists();
    }
}
