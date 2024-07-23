package io.jenkins.plugins.sample;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.Computer;
import hudson.model.TaskListener;
import io.jenkins.plugins.sample.cmd.*;
import io.jenkins.plugins.sample.cmd.help.Channel;
import io.jenkins.plugins.sample.cmd.help.ReceiveEmulatorPortTask;
import io.jenkins.plugins.sample.cmd.model.ADBDevice;
import io.jenkins.plugins.sample.cmd.model.AVDevice;
import io.jenkins.plugins.sample.cmd.model.EmulatorConfig;
import io.jenkins.plugins.sample.cmd.model.SDKPackages;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jvnet.hudson.plugins.port_allocator.PortAllocationManager;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

        if (avdHome == null || avdHome.isEmpty()) {
            avdHome = Constants.DIR_AVD_HOME;
        }

        // 通过 sdkmanager --list 获取已安装的 sdk 列表
        SDKPackages packages = SDKManagerCLIBuilder.withSDKRoot(sdkRoot)
            .createExecutable(launcher, workspace)
            .setChannel(Channel.STABLE)
            .setProxy(proxy)
            .list()
            .withEnv(env)
            .execute();
        listener.getLogger().println("SDK Manager is reading installed components");

        for (String s : getComponents()) {
            System.out.println("package: " + s + "\n");
        }
        // 在已安装列表中是否已存在用户所需要的版本，如 android-30
        Set<String> components = getComponents();
        packages.getInstalled().forEach(p -> components.remove(p.getId()));
        if (!components.isEmpty()) {
            SDKManagerCLIBuilder.withSDKRoot(sdkRoot)
                    .createExecutable(launcher, workspace)
                    .setProxy(proxy)
                    .installSDK(components)
                    .withEnv(env)
                    .execute();
            listener.getLogger().println("SDK Manager is installing " + StringUtils.join(components, ' '));
        }

        // 查看已经存在的模拟器列表
        List<AVDevice> devices = AVDManagerCLIBuilder.withSdkRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .silent(true)
                .listAVD()
                .withEnv(env)
                .execute();

        if (devices.stream().anyMatch(d -> config.getEmulatorName().equals(d.getName()))) {
            listener.getLogger().println("Android Virtual Device " + config.getEmulatorName() + " already exist, removing...");

            AVDManagerCLIBuilder.withSdkRoot(sdkRoot)
                    .createExecutable(launcher, workspace)
                    .silent(true)
                    .deleteAVD(config.getEmulatorName())
                    .withEnv(env)
                    .execute();
        }

        // create new device
        listener.getLogger().println("AVD Manager is creating a new device named " + config.getEmulatorName() + " using sysimage "
                + getSystemComponent());

        AVDManagerCLIBuilder.withSdkRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .silent(true)
                .packagePath(getSystemComponent())
                .createAVD(config)
                .withEnv(env)
                .execute();

        // create AVD descriptor file
        writeConfigFile(new FilePath(launcher.getChannel(), avdHome));

        // start ADB service
        ADBManagerCLIBuilder.withSDKRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .setMaxEmulators(1)
                .setPort(config.getAdbServerPort())
                .start()
                .withEnv(env)
                .execute();

        // start emulator
        EmulatorManagerCLIBuilder.withSdkRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .setDataDir(avdHome)
                .setEmulatorConfig(config)
                .setMode(EmulatorManagerCLIBuilder.SNAPSHOT.NOT_PERSIST)
                .start(listener)
                .withEnv(env)
                .executeAsync(listener);

        Integer port = workspace.act(new ReceiveEmulatorPortTask(config.getEmulatorReportConsolePort(), config.getEmulatorConnectToAdbTimeout()));
        if (port <= 0) {
            throw new IOException(Messages.EMULATOR_DID_NOT_START()); // FIXME
        }

        // emulator devices
        ADBManagerCLIBuilder.withSDKRoot(sdkRoot)
                .createExecutable(launcher, workspace)
                .setMaxEmulators(1)
                .setPort(config.getAdbServerPort())
                .listEmulatorDevices()
                .withEnv(env)
                .executeAsync(listener);
        listener.getLogger().println("waiting to emulator connect to adb port: " + port + " successfully");

        waitForEmulatorToBeReady(launcher, workspace, listener, sdkRoot, env);

    }

    private void waitForEmulatorToBeReady(Launcher launcher, FilePath workspace, TaskListener listener, String sdkRoot, EnvVars env) throws InterruptedException, IOException {
        int maxAttempts = 30;
        int attempt = 0;
        boolean isBooted = false;
        boolean isDeviceOnline = false;
        // wait for emulator
        String emulatorName = Constants.EMULATOR_NAME_PREFIX + config.getEmulatorConsolePort();

        ADBManagerCLIBuilder adbManagerCLIBuilder = ADBManagerCLIBuilder
                .withSDKRoot(sdkRoot)
                .setSerial(emulatorName)
                .createExecutable(launcher, workspace);
        ChristelleCLICommand<Boolean> emulatorIsBooted = adbManagerCLIBuilder.emulatorIsBooted().withEnv(env);
        ChristelleCLICommand<List<ADBDevice>> emulatorDevicesCommand = adbManagerCLIBuilder.listEmulatorDevices().withEnv(env);
        while (attempt < maxAttempts && (!isBooted || !isDeviceOnline)) {

            try {
                isBooted = emulatorIsBooted.executeAsyncReturnData(listener, launcher);
                listener.getLogger().println("Checking emulator status, attempt isBooted: " + isBooted);
                List<ADBDevice> adbDevices = emulatorDevicesCommand.execute();
                for (ADBDevice adbDevice : adbDevices) {
                    if (adbDevice.getEmulatorName().equals(emulatorName) && adbDevice.getStatus().equals("device")) {
                        isDeviceOnline = true;
                        break;
                    }
                }
            }catch (IOException e) {
                listener.getLogger().println(e.getMessage());
            }

            if (!isBooted || !isDeviceOnline) {
                Thread.sleep(3000); // Wait for 3 seconds before next check
                attempt++;
            }
            listener.getLogger().println(" ...wait...");
        }
        listener.getLogger().println("Emulator had Ready !!!");
        if (!isBooted || !isDeviceOnline) {
            throw new IOException("Emulator did not start or connect to ADB in the given time.");
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void writeConfigFile(FilePath avdHome) throws IOException, InterruptedException {
        FilePath advPath = avdHome.child(config.getEmulatorName() + ".avd");
        FilePath advConfig = avdHome.child(config.getEmulatorName() + ".ini");

        String remoteAVDPath = advPath.getRemote();
        String remoteAndroidHome = Objects.requireNonNull(avdHome.getParent()).getRemote();

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

    private void cleanupPort() {
        final Computer computer = Computer.currentComputer();
        PortAllocationManager portAllocator = PortAllocationManager.getManager(computer);
        if (portAllocator != null) {
            portAllocator.free(config.getAdbServerPort());
            portAllocator.free(config.getEmulatorConsolePort());
            portAllocator.free(config.getEmulatorReportConsolePort());
            portAllocator.free(config.getEmulatorADBConnectPort());
        }
    }

}
