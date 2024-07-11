package io.jenkins.plugins.sample.cmd.help;

public enum ToolsCommand {

    SDK_MANAGER("sdkmanager", ".bat"),
    EMULATOR_MANAGER("emulator", ".bat"),
    AVD_MANAGER("avdmanager", ".bat");

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
