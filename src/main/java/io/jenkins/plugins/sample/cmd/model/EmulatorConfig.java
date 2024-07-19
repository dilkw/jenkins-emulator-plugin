package io.jenkins.plugins.sample.cmd.model;

import hudson.Util;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.Messages;
import org.apache.commons.lang.LocaleUtils;

import java.util.*;

public class EmulatorConfig {

    private String emulatorName;
    private String targetABI;
    private String density;
    private String resolution;
    private String deviceLocale;
    private String deviceDefinition;
    private String avdAndroidAPI;
    private String dataDir;
    private String sdCardSize;
    private List<HardwareProperty> hardwareProperties;
    // adb server port，作用于adb工具，eg: adb -P 5037 start server ， 默认为5037
    private int adbServerPort = Constants.ADB_DEFAULT_SERVER_PORT;

    /**
     * Emulator
     * -avd test
     * -no-snapshot-save
     * -no-audio
     * -prop persist.sys.language=en
     * -prop persist.sys.country=US
     * -ports 5554,5555
     * -accel auto
     * -no-window
     * -no-boot-anim
     * -report-console tcp:55000,max=60000
     */
    // emulator report console port and connect to adb timeout, 作用于 emulator 工具 eg： -report-console tcp:55000,max=60000
    private int emulatorReportConsolePort = 50000;
    private int emulatorConnectToAdbTimeout;

    // emulator console port and adb connect port, 作用于 emulator 工具 eg： -ports 5554（emulatorConsolePort）,5555（emulatorADBConnectPort）
    private int emulatorConsolePort = Constants.EMULATOR_DEFAULT_CONSOLE_PORT;
    private int emulatorADBConnectPort = Constants.EMULATOR_DEFAULT_ADB_CONNECT_PORT;

    boolean wipeData;
    boolean showWindow;
    boolean useSnapshots;
    String commandLineOptions;
    String androidSdkHome;
    String executable;
    String avdNameSuffix;

    public EmulatorConfig(){}

    private EmulatorConfig(String emulatorName, boolean wipeData, boolean showWindow,
                           boolean useSnapshots, String commandLineOptions, String androidSdkHome, String executable, String
                                   avdNameSuffix) {
        this.emulatorName = emulatorName;
        this.wipeData = wipeData;
        this.showWindow = showWindow;
        this.useSnapshots = useSnapshots;
        this.commandLineOptions = commandLineOptions;
        this.androidSdkHome = androidSdkHome;
        this.executable = executable;
        this.avdNameSuffix = avdNameSuffix;
    }

    private EmulatorConfig(String osVersion, String screenDensity, String screenResolution,
                           String deviceLocale, String sdCardSize, boolean wipeData, boolean showWindow,
                           boolean useSnapshots, String commandLineOptions, String targetAbi, String deviceDefinition,
                           String androidSdkHome, String executable, String avdNameSuffix)
            throws IllegalArgumentException {
        if (osVersion == null || screenDensity == null || screenResolution == null) {
            throw new IllegalArgumentException("Valid OS version and screen properties must be supplied.");
        }

        // Normalise incoming variables
        int targetLength = osVersion.length();
        if (targetLength > 2 && osVersion.startsWith("\"") && osVersion.endsWith("\"")) {
            osVersion = osVersion.substring(1, targetLength - 1);
        }
        screenDensity = screenDensity.toLowerCase();
        if (screenResolution.matches("(?i)"+ Constants.REGEX_SCREEN_RESOLUTION_ALIAS)) {
            screenResolution = screenResolution.toUpperCase();
        } else if (screenResolution.matches("(?i)"+ Constants.REGEX_SCREEN_RESOLUTION)) {
            screenResolution = screenResolution.toLowerCase();
        }
        if (deviceLocale != null && deviceLocale.length() > 4) {
            deviceLocale = deviceLocale.substring(0, 2).toLowerCase() +"_"
                    + deviceLocale.substring(3).toUpperCase();
        }

        this.avdAndroidAPI = osVersion;
        this.density =screenDensity;
        this.resolution = screenResolution;
        this.deviceLocale = deviceLocale;
        this.sdCardSize = sdCardSize;
        this.wipeData = wipeData;
        this.showWindow = showWindow;
        this.useSnapshots = useSnapshots;
        this.commandLineOptions = commandLineOptions;
        if (targetAbi != null && targetAbi.startsWith("default/")) {
            targetAbi = targetAbi.replace("default/", "");
        }
        this.targetABI = targetAbi;
        this.deviceDefinition = deviceDefinition;
        this.androidSdkHome = androidSdkHome;
        this.executable = executable;
        this.avdNameSuffix = avdNameSuffix;
    }

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

    public void setSdCardSize(String sdCardSize) {
        this.sdCardSize = Util.fixEmptyAndTrim(sdCardSize);;
    }

    public int getEmulatorReportConsolePort() {
        return emulatorReportConsolePort;
    }

    public void setEmulatorReportConsolePort(int emulatorReportConsolePort) {
        this.emulatorReportConsolePort = emulatorReportConsolePort;
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

    public String getSdCardSize() {
        return sdCardSize;
    }

    public int getAdbServerPort() {
        return adbServerPort;
    }

    public void setAdbServerPort(int adbServerPort) {
        this.adbServerPort = adbServerPort;
    }

    public int getEmulatorConnectToAdbTimeout() {
        return emulatorConnectToAdbTimeout;
    }

    public void setEmulatorConnectToAdbTimeout(int emulatorConnectToAdbTimeout) {
        this.emulatorConnectToAdbTimeout = emulatorConnectToAdbTimeout;
    }

    public String getDataDir() {
        return dataDir;
    }

    public int getEmulatorADBConnectPort() {
        return emulatorADBConnectPort;
    }

    public void setEmulatorADBConnectPort(int emulatorADBConnectPort) {
        this.emulatorADBConnectPort = emulatorADBConnectPort;
    }

    public int getEmulatorConsolePort() {
        return emulatorConsolePort;
    }

    public void setEmulatorConsolePort(int emulatorConsolePort) {
        this.emulatorConsolePort = emulatorConsolePort;
    }

    public boolean isWipeData() {
        return wipeData;
    }

    public void setWipeData(boolean wipeData) {
        this.wipeData = wipeData;
    }

    public boolean isShowWindow() {
        return showWindow;
    }

    public void setShowWindow(boolean showWindow) {
        this.showWindow = showWindow;
    }

    public boolean isUseSnapshots() {
        return useSnapshots;
    }

    public void setUseSnapshots(boolean useSnapshots) {
        this.useSnapshots = useSnapshots;
    }

    public String getCommandLineOptions() {
        return commandLineOptions;
    }

    public void setCommandLineOptions(String commandLineOptions) {
        this.commandLineOptions = commandLineOptions;
    }

    public String getAvdNameSuffix() {
        return avdNameSuffix;
    }

    public void setAvdNameSuffix(String avdNameSuffix) {
        this.avdNameSuffix = avdNameSuffix;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getAndroidSdkHome() {
        return androidSdkHome;
    }

    public void setAndroidSdkHome(String androidSdkHome) {
        this.androidSdkHome = androidSdkHome;
    }

    public static String getAvdName(String avdName, String osVersion, String screenDensity,
                                    String screenResolution, String deviceLocale, String targetAbi, String deviceDefinition,
                                    String avdNameSuffix) {
        try {
            return create(avdName, osVersion, screenDensity, screenResolution, deviceLocale, null, false, false, false,
                    null, targetAbi, deviceDefinition, null, null, avdNameSuffix).getAvdName();
        } catch (IllegalArgumentException e) {}
        return null;
    }

    public static EmulatorConfig create(String avdName, String osVersion, String screenDensity,
                                              String screenResolution, String deviceLocale, String sdCardSize, boolean wipeData,
                                              boolean showWindow, boolean useSnapshots, String commandLineOptions, String targetAbi,
                                              String deviceDefinition, String androidSdkHome, String executable, String avdNameSuffix) {
        if (Util.fixEmptyAndTrim(avdName) == null) {
            return new EmulatorConfig(osVersion, screenDensity, screenResolution, deviceLocale, sdCardSize, wipeData,
                    showWindow, useSnapshots, commandLineOptions, targetAbi, deviceDefinition, androidSdkHome, executable, avdNameSuffix);
        }

        return new EmulatorConfig(avdName, wipeData, showWindow, useSnapshots, commandLineOptions, androidSdkHome, executable,
                avdNameSuffix);
    }

    public boolean isNamedEmulator() {
        return emulatorName != null && avdAndroidAPI == null;
    }
    public String getAvdName() {
        if (isNamedEmulator()) {
            return emulatorName;
        }

        return getGeneratedAvdName();
    }

    private String getGeneratedAvdName() {
        String locale = getDeviceLocale().replace('_', '-');
        String density = this.density;
        String resolution = this.resolution;
        String platform = avdAndroidAPI.replaceAll("[^a-zA-Z0-9._-]", "_");
        String abi = "";
        if (targetABI != null) {
            abi = "_" + targetABI.replaceAll("[^a-zA-Z0-9._-]", "-");
        }
        String deviceDef = "";
        if (deviceDefinition != null && !deviceDefinition.isEmpty()) {
            deviceDef = "_" + deviceDefinition.replaceAll("[^a-zA-Z0-9._-]", "-");
        }
        String suffix = "";
        if (avdNameSuffix != null) {
            suffix = "_" + avdNameSuffix.replaceAll("[^a-zA-Z0-9._-]", "-");
        }

        return String.format("hudson_%s_%s_%s_%s%s%s%s", locale, density, resolution, platform, abi, deviceDef, suffix);
    }

    public Collection<ValidationError> validate() {
        Collection<ValidationError> errors = new ArrayList<>();
        if (avdAndroidAPI == null) {
            errors.add(new ValidationError("osVersion is required"));
        }
        if (ScreenDensity.valueOfDensity(density) == null) {
            errors.add(new ValidationError("screen density '" + density + "' not valid"));
        }
        if (ScreenResolution.valueOf(resolution) == null) {
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
        if (sdCardSize != null) {
            try {
                if (Integer.parseInt(sdCardSize) < 9) {
                    errors.add(new ValidationError(Messages.AndroidEmulatorBuild_sdCardTooSmall()));
                }
            } catch (NumberFormatException e) {
                errors.add(new ValidationError("Invalid SD card size " + sdCardSize));
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

        @Override
        public String toString() {
            return message;
        }
    }
}
