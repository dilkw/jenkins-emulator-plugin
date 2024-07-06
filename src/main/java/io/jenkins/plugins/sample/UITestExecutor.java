package io.jenkins.plugins.sample;

import java.io.IOException;

public class UITestExecutor {

    public void runTests(String avdName) throws IOException, InterruptedException {
        String[] command = {
            "/path/to/android-sdk/platform-tools/adb",
            "-s",
            avdName,
            "shell",
            "am",
            "instrument",
            "-w",
            "com.example.android.test/android.support.test.runner.AndroidJUnitRunner"
        };
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.start().waitFor();
    }
}
