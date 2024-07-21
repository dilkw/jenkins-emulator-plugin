package io.jenkins.plugins.sample.cmd.help;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.sample.Constants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    // 根据所需要的工具返回对应的路径
    public static String findInSdkToolsHome(final boolean useLegacySdkStructure, ToolsCommand toolsCommand, boolean isUnix) {
        String toolsHome = null;
        switch (toolsCommand) {
            case AVD_MANAGER:
            case SDK_MANAGER:
                if (useLegacySdkStructure) {
                    toolsHome = Constants.DIR_TOOLS_BIN;
                }else {
                    toolsHome = Constants.DIR_CMDLINE_TOOLS_BIN;
                }
                break;
            case EMULATOR:
                if (useLegacySdkStructure) {
                    toolsHome = Constants.DIR_TOOLS;
                } else {
                    toolsHome = Constants.DIR_EMULATOR;
                }
                break;
            case ADB:
                toolsHome = Constants.DIR_PLATFORM_TOOL;
                break;
        }

        if (!isUnix) {
            toolsHome =  StringUtils.replace(toolsHome, "/", "\\");
        }
        return toolsHome;
    }

    public static String getExecutable(boolean isUnix, String sdkRoot, ToolsCommand toolsCommand) {
        File toolHome = new File(sdkRoot, findInSdkToolsHome(true, toolsCommand, isUnix));
        if (!toolHome.exists()) {
            toolHome = new File(sdkRoot, findInSdkToolsHome(false, toolsCommand, isUnix));
        }
        File cmd = new File(toolHome, toolsCommand.getExecutable(isUnix));
        System.out.println("cmd: " + cmd.getPath());
        if (cmd.exists()) {
            return cmd.getPath();
        }
        return null;
    }

    public static String getExecutable(Platform platform, String sdkRoot, ToolsCommand toolsCommand) {
        boolean isUnix = platform == Platform.LINUX;
        File toolHome = new File(sdkRoot, findInSdkToolsHome(true, toolsCommand, isUnix));
        if (!toolHome.exists()) {
            toolHome = new File(sdkRoot, findInSdkToolsHome(false, toolsCommand, isUnix));
        }
        File cmd = new File(toolHome, toolsCommand.getExecutable(platform == Platform.LINUX));
        System.out.println("cmd: " + cmd.getPath());
        if (cmd.exists()) {
            return cmd.getPath();
        }
        return null;
    }


    public static FilePath createExecutable(final Launcher launcher, FilePath workspace, String sdkRoot, ToolsCommand toolsCommand) throws InterruptedException, IOException {
        Platform platform = Platform.fromWorkspace(workspace);
        return getFilePath(launcher, platform, sdkRoot, toolsCommand);
    }


    public static FilePath createExecutable(final Launcher launcher, Platform platform, String sdkRoot, ToolsCommand toolsCommand) throws InterruptedException, IOException {
        return getFilePath(launcher, platform, sdkRoot, toolsCommand);
    }

    public static FilePath getFilePath(Launcher launcher, Platform platform, String sdkRoot, ToolsCommand toolsCommand) throws IOException {
        String executableString = Utils.getExecutable(platform, sdkRoot, toolsCommand);
        final VirtualChannel channel = launcher.getChannel();
        return createFilePath(channel, sdkRoot, toolsCommand, executableString);
    }

    public static FilePath getFilePath(VirtualChannel channel, boolean isUnix, String sdkRoot, ToolsCommand toolsCommand) throws IOException {
        String executableString = Utils.getExecutable(isUnix, sdkRoot, toolsCommand);
        return createFilePath(channel, sdkRoot, toolsCommand, executableString);
    }

    private static FilePath createFilePath(VirtualChannel channel, String sdkRoot, ToolsCommand toolsCommand, String executableString) throws IOException {
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


    public static String expandVariables(EnvVars envVars, Map<String,String> buildVars,
                                         String token) {
        final Map<String,String> vars = new HashMap<String,String>(envVars);
        if (buildVars != null) {
            // Build-specific variables, if any, take priority over environment variables
            vars.putAll(buildVars);
        }

        String result = Util.fixEmptyAndTrim(token);
        if (result != null) {
            result = Util.replaceMacro(result, vars);
        }
        return Util.fixEmptyAndTrim(result);
    }

}
