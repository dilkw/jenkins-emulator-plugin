package io.jenkins.plugins.sample.cmd.help;

import hudson.FilePath;
import io.jenkins.plugins.sample.Constants;

import java.io.File;

public class Utils {

    public static String findInSdk(final boolean useLegacySdkStructure) {
        if (!useLegacySdkStructure) {
            return Constants.CMD_TOOLS_BIN_DIR;
        }
        return Constants.TOOLS_BIN_DIR;
    }

    public static FilePath getExecutable(Platform platform, String home, ToolsCommand toolsCommand) {
        File toolHome = new File(home, findInSdk(true));
        if (!toolHome.exists()) {
            toolHome = new File(home, findInSdk(false));
        }
        File cmd = new File(toolHome, toolsCommand.getExecutable(platform == Platform.LINUX));
        if (cmd.exists()) {
            return new FilePath(cmd);
        }
        return null;
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

}
