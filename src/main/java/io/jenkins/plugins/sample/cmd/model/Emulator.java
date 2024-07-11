package io.jenkins.plugins.sample.cmd.model;

public class Emulator {

    private final String emulatorName;
    private final String targetABI;
    private final String deviceLocale;
    private final String deviceDefinition;
    private final int avdAndroidAPI;

    public Emulator(String emulatorName, String targetABI, String deviceLocale, String deviceDefinition, int avdAndroidAPI) {
        this.emulatorName = emulatorName;
        this.targetABI = targetABI;
        this.deviceLocale = deviceLocale;
        this.deviceDefinition = deviceDefinition;
        this.avdAndroidAPI = avdAndroidAPI;
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

    public int getAvdAndroidAPI() {
        return avdAndroidAPI;
    }
}
