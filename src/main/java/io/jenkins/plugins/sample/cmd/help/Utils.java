package io.jenkins.plugins.sample.cmd.help;

import hudson.FilePath;
import hudson.Launcher;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.SDKManagerCLIBuilder;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static String findInSdk(final boolean useLegacySdkStructure) {
        if (!useLegacySdkStructure) {
            return Constants.CMD_TOOLS_BIN_DIR;
        }
        return Constants.TOOLS_BIN_DIR;
    }

    public static String getExecutable(Platform platform, String home, ToolsCommand toolsCommand) {
        File toolHome = new File(home, findInSdk(true));
        if (!toolHome.exists()) {
            toolHome = new File(home, findInSdk(false));
        }
        File cmd = new File(toolHome, toolsCommand.getExecutable(platform == Platform.LINUX));
        if (cmd.exists()) {
            return cmd.getPath();
        }
        return null;
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    public static FilePath createExecutable(final Launcher launcher, FilePath workspace, String toolRoot) throws InterruptedException, IOException {
        Platform platform = Platform.fromWorkspace(workspace);
        String executableString = Utils.getExecutable(platform, toolRoot, ToolsCommand.SDK_MANAGER);
        final VirtualChannel channel = launcher.getChannel();
        if (channel == null) {
            throw new IOException("Unable to get a channel for the launcher");
        }
        if (executableString == null || executableString.isEmpty()) {
            throw new IOException("Unable to get a path for the sdk root");
        }
        return new FilePath(channel, executableString);
    }

}
