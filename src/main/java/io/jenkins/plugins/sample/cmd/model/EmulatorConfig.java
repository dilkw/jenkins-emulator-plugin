package io.jenkins.plugins.sample.cmd.model;

import hudson.Util;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.Messages;
import org.apache.commons.lang.LocaleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class EmulatorConfig {

    private String emulatorName;
    private String targetABI;
    private String density;
    private String resolution;
    private String deviceLocale;
    private String deviceDefinition;
    private String avdAndroidAPI;
    private String dataDir;
    private String sdkCardSize;
    private int reportConsolePort;
    private List<HardwareProperty> hardwareProperties;
    private int adbServerPort = Constants.ADB_DEFAULT_SERVER_PORT;
    private int adbConnectionTimeout;

    public void setEmulatorName(String emulatorName) {
        this.emulatorName = Util.fixEmptyAndTrim(emulatorName);
    }

    public void setTargetABI(String targetABI) {
        this.targetABI = Util.fixEmptyAndTrim(targetABI);
    }

    public void setDeviceLocale(String deviceLocale) {
        this.deviceLocale = Util.fixEmptyAndTrim(deviceLocale);
    }

    public List<HardwareProperty> getHardwareProperties() {
        return hardwareProperties;
    }

    public void setHardwareProperties(List<HardwareProperty> hardwareProperties) {
        this.hardwareProperties = hardwareProperties;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = Util.fixEmptyAndTrim(resolution);
    }

    public void setDeviceDefinition(String deviceDefinition) {
        this.deviceDefinition = Util.fixEmptyAndTrim(deviceDefinition);
    }

    public void setAvdAndroidAPI(String avdAndroidAPI) {
        this.avdAndroidAPI = Util.fixEmptyAndTrim(avdAndroidAPI);
    }

    public void setSdkCardSize(String sdkCardSize) {
        this.sdkCardSize = Util.fixEmptyAndTrim(sdkCardSize);;
    }

    public int getReportConsolePort() {
        return reportConsolePort;
    }

    public void setReportConsolePort(int reportConsolePort) {
        this.reportConsolePort = reportConsolePort;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public String getEmulatorName() {
        return this.emulatorName;
    }

    public String getTargetABI() {
        return this.targetABI;
    }

    public String getDeviceLocale() {
        return deviceLocale;
    }

    public String getDeviceDefinition() {
        return deviceDefinition;
    }

    public String getAvdAndroidAPI() {
        return avdAndroidAPI;
    }

    public String getSdkCardSize() {
        return sdkCardSize;
    }

    public int getAdbServerPort() {
        return adbServerPort;
    }

    public void setAdbServerPort(int adbServerPort) {
        this.adbServerPort = adbServerPort;
    }

    public int getAdbConnectionTimeout() {
        return adbConnectionTimeout;
    }

    public void setAdbConnectionTimeout(int adbConnectionTimeout) {
        this.adbConnectionTimeout = adbConnectionTimeout;
    }

    public int getReportPort() {
        return reportConsolePort;
    }

    public String getDataDir() {
        return dataDir;
    }

    public Collection<ValidationError> validate() {
        Collection<ValidationError> errors = new ArrayList<>();
        if (avdAndroidAPI == null) {
            errors.add(new ValidationError("osVersion is required"));
        }
        if (ScreenDensity.valueOfDensity(density) == null) {
            errors.add(new ValidationError("screen density '" + density + "' not valid"));
        }
        if (ScreenResolution.valueOfResolution(resolution) == null) {
            errors.add(new ValidationError("screen resolution '" + resolution + "' not valid"));
        }
        if (targetABI == null) {
            errors.add(new ValidationError("Target ABI is required"));
        }
        if (deviceLocale != null) {
            try {
                // parse locale with _ or -
                if (Util.fixEmpty(Locale.forLanguageTag(deviceLocale).getLanguage()) != null || LocaleUtils.toLocale(deviceLocale) != null) {
                    // it's ok
                }
            } catch (IllegalArgumentException e) {
                errors.add(new ValidationError("Invalid locale format " + deviceLocale));
            }
        }
        if (sdkCardSize != null) {
            try {
                if (Integer.parseInt(sdkCardSize) < 9) {
                    errors.add(new ValidationError(Messages.AndroidEmulatorBuild_sdCardTooSmall()));
                }
            } catch (NumberFormatException e) {
                errors.add(new ValidationError("Invalid SD card size " + sdkCardSize));
            }
        }
        return errors;
    }

    public static class ValidationError {
        private final String message;

        public ValidationError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
