package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.remoting.VirtualChannel;
import io.jenkins.plugins.sample.cmd.ADBManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.help.Platform;
import io.jenkins.plugins.sample.cmd.model.AndroidEmulatorShareDataAction;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.Objects;

@Extension
public class EmulatorToolsKillListener extends RunListener<Run<?, ?>> {

    @Override
    public void onCompleted(Run<?, ?> run, @NonNull TaskListener listener) {
        listener.getLogger().println("RunListener onCompleted");
        AndroidEmulatorShareDataAction shareDataAction = run.getAction(AndroidEmulatorShareDataAction.class);
        if (shareDataAction == null) {
            System.out.println("shareDataAction is null");
            return;
        }

        String emulatorPort = String.valueOf(shareDataAction.getEmulatorConsolePort());
        Node node = Objects.requireNonNull(run.getExecutor()).getOwner().getNode();
        try {
            EnvVars envVars = run.getEnvironment(listener);
            String sdkRoot = envVars.get(Constants.ENV_VAR_ANDROID_SDK_ROOT);
            FilePath filePath = null;
            VirtualChannel channel = null;
            Platform platform = null;
            if (node != null) {
                System.out.println("node is not null");
                channel = node.getChannel();
                filePath = new FilePath(channel, sdkRoot);
                platform = Platform.fromWorkspace(filePath);
            }
            if (channel == null) {
                System.out.println("channel is null");
                return;
            }
            Launcher launcher = new Launcher.RemoteLauncher(listener, channel, platform == Platform.LINUX);
            ADBManagerCLIBuilder adbManagerCLIBuilder = ADBManagerCLIBuilder.withSDKRoot(sdkRoot)
                    .addEnvVars(envVars)
                    .createExecutable(launcher, filePath);

            // 关闭 emulator
            adbManagerCLIBuilder.killEmulatorByPort(emulatorPort).execute();
            // 关闭 adb server
            adbManagerCLIBuilder.stop();
            listener.getLogger().println("killServiceAfterBuild tearDown");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.onCompleted(run, listener);
    }

    @Override
    public void onDeleted(Run<?, ?> run) {
        super.onDeleted(run);
    }
}
