/*
 * The MIT License
 *
 * Copyright (c) 2020, Nikolas Falco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.TaskListener;
import io.jenkins.plugins.sample.cmd.ADBManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.AVDManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.EmulatorManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.SDKManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.help.Channel;
import io.jenkins.plugins.sample.cmd.help.ReceiveEmulatorPortTask;
import io.jenkins.plugins.sample.cmd.model.AVDevice;
import io.jenkins.plugins.sample.cmd.model.EmulatorConfig;
import io.jenkins.plugins.sample.cmd.model.SDKPackages;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EmulatorRunner {

    private final EmulatorConfig config;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
    public EmulatorRunner(@NonNull EmulatorConfig config) {
        this.config = config;
    }

    public void run(@NonNull FilePath workspace,
                    @NonNull TaskListener listener,
                    @Nullable EnvVars env) throws IOException, InterruptedException {
        Launcher launcher = workspace.createLauncher(listener);
        if (env == null) {
            env = new EnvVars();
        }

        ProxyConfiguration proxy = Jenkins.get().proxy;

        String avdHome = env.get(Constants.ENV_ANDROID_AVD_HOME);
        String sdkRoot = env.get(Constants.ENV_VAR_ANDROID_SDK_ROOT); // FIXME required!

        // 通过 sdkmanager --list 获取已安装的 sdk 列表
        SDKPackages packages = SDKManagerCLIBuilder.withSDKRoot(sdkRoot)
            .createExecutable(launcher, workspace)
            .setChannel(Channel.STABLE)
            .setProxy(proxy)
            .addEnvironment(env)
            .list()
            .execute();
        listener.getLogger().println("SDK Manager is reading installed components");

        // 在已安装列表中是否已存在用户所需要的版本，如 android-30
        Set<String> components = getComponents();
        packages.getInstalled().forEach(p -> components.remove(p.getId()));
        if (!components.isEmpty()) {
            SDKManagerCLIBuilder.withSDKRoot(sdkRoot)
                    .createExecutable(launcher, workspace)
                    .setProxy(proxy)
                    .addEnvironment(env)
                    .installSDK(components)
                    .execute();
            listener.getLogger().println("SDK Manager is installing " + StringUtils.join(components, ' '));
        }

        // 查看已经存在的模拟器列表
        List<AVDevice> devices = AVDManagerCLIBuilder.withSdkRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .addEnv(env)
                .silent(true)
                .listAVD()
                .execute();

        if (devices.stream().anyMatch(d -> config.getEmulatorName().equals(d.getName()))) {
            listener.getLogger().println("Android Virtual Device " + config.getEmulatorName() + " already exist, removing...");

            AVDManagerCLIBuilder.withSdkRoot(sdkRoot)
                    .createExecutable(launcher, workspace)
                    .addEnv(env)
                    .silent(true)
                    .deleteAVD(config.getEmulatorName())
                    .execute();
        }

        // create new device
        listener.getLogger().println("AVD Manager is creating a new device named " + config.getEmulatorName() + " using sysimage "
                + getSystemComponent());

        AVDManagerCLIBuilder.withSdkRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .addEnv(env)
                .silent(true)
                .packagePath(getSystemComponent())
                .createAVD(config)
                .execute();

        // create AVD descriptor file
        writeConfigFile(new FilePath(launcher.getChannel(), avdHome));

        // start ADB service
        ADBManagerCLIBuilder.withSDKRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .addEnvVars(env) //
                .setMaxEmulators(1) // FIXME set equals to the number of node executors
                .setPort(config.getAdbServerPort()) //
                .start() //
                .execute();

        // start emulator
        EmulatorManagerCLIBuilder.withSdkRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .setEmulatorConfig(config)
                .setMode(EmulatorManagerCLIBuilder.SNAPSHOT.NOT_PERSIST)
                .start()
                .executeAsync(listener);

        Integer port = workspace.act(new ReceiveEmulatorPortTask(config.getReportPort(), config.getAdbConnectionTimeout()));
        if (port <= 0) {
            throw new IOException(Messages.EMULATOR_DID_NOT_START()); // FIXME
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void writeConfigFile(FilePath avdHome) throws IOException, InterruptedException {
        FilePath advPath = avdHome.child(config.getEmulatorName() + ".avd");
        FilePath advConfig = avdHome.child(config.getEmulatorName() + ".ini");

        String remoteAVDPath = advPath.getRemote();
        String remoteAndroidHome = avdHome.getParent().getRemote();

        advConfig.touch(new Date().getTime());
        String content = "avd.ini.encoding=UTF-8\n" + 
                "path=" + remoteAVDPath + "\n" +
                "path.rel=" + remoteAVDPath.substring(remoteAndroidHome.length() + 1) + "\n" +
                "target=" + config.getAvdAndroidAPI();
        advConfig.write(content, "UTF-8");
    }

    private Set<String> getComponents() {
        Set<String> components = new LinkedHashSet<>();
        components.add(buildComponent("platforms", config.getAvdAndroidAPI()));
        components.add(getSystemComponent());
        return components;
    }

    private String getSystemComponent() {
        return buildComponent("system-images", config.getAvdAndroidAPI(), "default", config.getTargetABI());
    }

    private String buildComponent(String...parts) {
        return StringUtils.join(parts, ';');
    }

}
