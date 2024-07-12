package io.jenkins.plugins.sample.cmd;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ADBManagerCLIBuilder {

    private static final String ARG_START_SERVER = "start-server";
    private static final String ARG_KILL_SERVER = "kill-server";

    private String sdkRoot = "";

    private FilePath executable;
    private EnvVars env;
    private ArgumentListBuilder args;
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
        if (envVars == null) {
            this.env = new EnvVars();
        }
        this.env.putAll(envVars);
        return this;
    }

    public ChristelleCLICommand<Void> start() {
        ArgumentListBuilder arguments = buildGlobalOptions();
        arguments.add(ARG_START_SERVER);

        return new ChristelleCLICommand<>(executable, arguments, buildEnvVars());
    }

    public static ADBManagerCLIBuilder withSDKRoot(String sdkRoot) {
        return new ADBManagerCLIBuilder(sdkRoot);
    }

    public ADBManagerCLIBuilder createExecutable(final Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        String toolRoot = sdkRoot + Constants.PLATFORM_TOOLS_DIR;
        executable = Utils.createExecutable(launcher, workspace, toolRoot);
        return this;
    }

    public ChristelleCLICommand<Void> waitForDevice() {
        return new ChristelleCLICommand<>(executable, args, env);
    }

    public ChristelleCLICommand<Void> buildCommand() {
        return new ChristelleCLICommand<>(executable, args, env);
    }

    public ArgumentListBuilder getArguments() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        return builder;
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
