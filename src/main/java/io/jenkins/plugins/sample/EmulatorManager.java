package io.jenkins.plugins.sample;

import java.io.IOException;

public class EmulatorManager {
    private static final String ANDROID_SDK_PATH = "/path/to/android-sdk";

    public void startEmulator(String avdName) throws IOException, InterruptedException {
        String[] command = {ANDROID_SDK_PATH + "/emulator/emulator", "-avd", avdName, "-no-snapshot-load", "-no-window"
        };
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.start().waitFor();
    }

    public void stopEmulator(String avdName) throws IOException, InterruptedException {
        String[] command = {ANDROID_SDK_PATH + "/platform-tools/adb", "-s", avdName, "emu", "kill"};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.start().waitFor();
    }
}
