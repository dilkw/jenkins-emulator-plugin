package io.jenkins.plugins.sample.cmd;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.ToolsCommand;
import io.jenkins.plugins.sample.cmd.help.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ADBManagerCLIBuilder {

    private static final String ARG_START_SERVER = "start-server";
    private static final String ARG_KILL_SERVER = "kill-server";
    private static final String ARG_KILL_EMULATOR = "emu kill";

    private String sdkRoot = "";

    private FilePath executable;
    private EnvVars env;
    private ArgumentListBuilder argumentListBuilder;
    private String serial;
    private boolean trace = false;
    private int port = 5037;
    private int maxEmulator = 16;

    private final List<ChristelleCLICommand<Objects>> christelleCLICommandList = new ArrayList<>();

    ADBManagerCLIBuilder(String sdkRoot) {
        this.sdkRoot = sdkRoot;
    }

    public ADBManagerCLIBuilder setSerial(String serial) {
        this.serial = serial;
        return this;
    }

    public ADBManagerCLIBuilder setPort(int port) {
        if (port <= 1023) { // system ports
            throw new IllegalArgumentException("Invalid port " + port);
        }
        this.port = port;
        return this;
    }

    public ADBManagerCLIBuilder setMaxEmulators(int maxEmulator) {
        this.maxEmulator = maxEmulator;
        return this;
    }

    public ADBManagerCLIBuilder trace() {
        this.trace = true;
        return this;
    }

    public ADBManagerCLIBuilder addEnvVars(EnvVars envVars) {
        if (this.env == null) {
            this.env = new EnvVars();
        }
        this.env.putAll(envVars);
        return this;
    }

    // 启动 adb server
    public ChristelleCLICommand<Void> start() {
        ArgumentListBuilder arguments = buildGlobalOptions();
        arguments.add(ARG_START_SERVER);
        return new ChristelleCLICommand<>(executable, arguments, buildEnvVars());
    }

    // 停止 adb server
    public ChristelleCLICommand<Void> stop() {
        ArgumentListBuilder arguments = buildGlobalOptions();
        arguments.add(ARG_KILL_SERVER);
        return new ChristelleCLICommand<>(executable, arguments, buildEnvVars());
    }

    // adb kill emulator eg: adb -s test_api29 emu kill
    public ChristelleCLICommand<Void> killEmulatorByEmulatorName(String emulatorName) {
        ArgumentListBuilder arguments = new ArgumentListBuilder();
        if (serial != null) {
            arguments.add("-s", emulatorName);
        }
        arguments.add(ARG_KILL_EMULATOR);
        return new ChristelleCLICommand<>(executable, arguments, buildEnvVars());
    }
    // adb kill emulator eg: adb -s emulator-5554 emu kill
    public ChristelleCLICommand<Void> killEmulatorByPort(String...ports) {
        ArgumentListBuilder arguments = new ArgumentListBuilder();
//        if (serial != null) {
            if (ports.length > 0) {
                StringBuilder argumentsBuilder = new StringBuilder();
                for (int i = 0; i < ports.length; i++) {
                    argumentsBuilder.append("emulator-").append(ports[i]);
                    if (i != ports.length - 1) {
                        arguments.add(",");
                    }
                }
                arguments.add("-s", argumentsBuilder.toString());
            }
//        }
        arguments.add(ARG_KILL_EMULATOR);
        return new ChristelleCLICommand<>(executable, arguments, buildEnvVars());
    }

    // adb devices list
    public ChristelleCLICommand<Void> listEmulatorDevices() {
        ArgumentListBuilder arguments = new ArgumentListBuilder();
        arguments.add("devices");
        return new ChristelleCLICommand<>(executable, arguments, buildEnvVars());
    }

    public static ADBManagerCLIBuilder withSDKRoot(String sdkRoot) {
        return new ADBManagerCLIBuilder(sdkRoot);
    }

    public ADBManagerCLIBuilder createExecutable(final Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        executable = Utils.createExecutable(launcher, workspace, sdkRoot, ToolsCommand.ADB);
        return this;
    }

    public ChristelleCLICommand<Void> waitForDevice() {
        ArgumentListBuilder args = new ArgumentListBuilder();
        EnvVars envVars = buildEnvVars();
        if (executable == null) {
            return null;
        }
        return new ChristelleCLICommand<>(executable, args, envVars);
    }

    private ArgumentListBuilder buildGlobalOptions() {
        ArgumentListBuilder arguments = new ArgumentListBuilder();

        if (serial != null) {
            arguments.add("-s", serial);
        }

        arguments.add("-P", String.valueOf(port));
        return arguments;
    }

    private EnvVars buildEnvVars() {
        EnvVars env = new EnvVars();
        if (trace) {
            env.put(Constants.ENV_ADB_TRACE, "all,adb,sockets,packets,rwx,usb,sync,sysdeps,transport,jdwp");
        }
        env.put(Constants.ENV_ADB_LOCAL_TRANSPORT_MAX_PORT, String.valueOf(5553 + (maxEmulator * 2)));
        return env;
    }

}
