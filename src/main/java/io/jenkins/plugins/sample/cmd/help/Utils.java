package io.jenkins.plugins.sample.cmd.help;

import hudson.FilePath;
import hudson.Launcher;
import hudson.remoting.VirtualChannel;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.sample.Constants;
import org.springframework.core.style.ToStringStyler;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static String findInSdk(final boolean useLegacySdkStructure) {
        if (!useLegacySdkStructure) {
            return StringUtils.replace(Constants.TOOLS_BIN_DIR, "/", "\\");
        }
        return StringUtils.replace(Constants.CMD_TOOLS_BIN_DIR, "/", "\\");
    }

    public static String getExecutable(Platform platform, String sdkRoot, ToolsCommand toolsCommand) {
        File toolHome = new File(sdkRoot, findInSdk(true));
        if (!toolHome.exists()) {
            toolHome = new File(sdkRoot, findInSdk(false));
        }
        File cmd = new File(toolHome, toolsCommand.getExecutable(platform == Platform.LINUX));
        System.out.println("cmd: " + cmd.getPath());
        if (cmd.exists()) {
            return cmd.getPath();
        }
        return null;
    }

    public static boolean fileExists(String path) {
        return new File(path).exists();
    }

    public static FilePath createExecutable(final Launcher launcher, FilePath workspace, String sdkRoot, ToolsCommand toolsCommand) throws InterruptedException, IOException {
        Platform platform = Platform.fromWorkspace(workspace);
        String executableString = Utils.getExecutable(platform, sdkRoot, toolsCommand);
        final VirtualChannel channel = launcher.getChannel();
        if (channel == null) {
            throw new IOException("Unable to get a channel for the launcher");
        }
        if (executableString == null || executableString.isEmpty()) {
            throw new IOException("Unable to get a path for the sdk root: " + sdkRoot);
        }
        FilePath filePath = new FilePath(channel, executableString);
        System.out.println("-------------------------" + toolsCommand.name() + "---" + filePath.getRemote());
        return filePath;
    }

    public static String mergerPath(Platform platform, String...paths) {
        String newPath = StringUtils.join(paths, "/");
        if (platform == Platform.WINDOWS) {
            newPath = StringUtils.replace(newPath, "/", "\\");
            System.out.println("is Windows" + newPath);
        }
        return newPath;
    }

}
