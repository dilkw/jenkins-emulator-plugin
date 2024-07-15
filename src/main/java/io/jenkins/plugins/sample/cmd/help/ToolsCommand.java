package io.jenkins.plugins.sample.cmd.help;

public enum ToolsCommand {

    SDK_MANAGER("sdkmanager", ".bat"),
    AVD_MANAGER("avdmanager", ".bat"),
    EMULATOR("emulator", ".exe"),
    ADB("adb", ".exe"),
    ANDROID_LEGACY("android", ".bat"),
    EMULATOR_ARM("emulator-arm", ".exe"),
    EMULATOR_MIPS("emulator-mips", ".exe"),
    EMULATOR_X86("emulator-x86", ".exe"),
    EMULATOR64_ARM("emulator64-arm", ".exe"),
    EMULATOR64_MIPS("emulator64-mips", ".exe"),
    EMULATOR64_X86("emulator64-x86", ".exe"),
    MKSDCARD("mksdcard", ".exe");

    private final String toolName;
    private final String windowsExtension;

    ToolsCommand(String toolName, String windowsExtension) {
        this.toolName = toolName;
        this.windowsExtension = windowsExtension;
    }

    public String getExecutable(boolean isUnix) {
        if (isUnix) {
            return toolName;
        }
        return toolName + windowsExtension;
    }
}
