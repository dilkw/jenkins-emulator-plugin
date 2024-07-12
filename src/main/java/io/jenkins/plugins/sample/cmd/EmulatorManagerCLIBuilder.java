package io.jenkins.plugins.sample.cmd;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.model.EmulatorConfig;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public class EmulatorManagerCLIBuilder {

    private static final String ARG_NO_BOOT_ANIM = "-no-boot-anim";
    private static final String ARG_NO_AUDIO = "-no-audio";
    private static final String ARG_WIPE_DATA = "-wipe-data";
    private static final String ARG_PROP = "-prop";
    private static final String ARG_MEMORY = "-memory";
    private static final String ARG_PORTS = "-ports";
    private static final String ARG_ACCEL = "-accel";
    private static final String ARG_PROXY = "-http-proxy";
    private static final String ARG_NO_WINDOW = "-no-window";
    private static final String ARG_REPORT_CONSOLE = "-report-console";

    final String sdkRoot;
    private EmulatorConfig emulatorConfig;
    private FilePath executable;
    private CameraFront cameraFront;
    private CameraBack cameraBack;
    private SNAPSHOT mode;
    private int memory;
    private boolean wipe;
    private int consolePort;
    private Object adbPort;
    private ProxyConfiguration proxy;

    public enum SNAPSHOT {
        NONE("-no-snapshot"),
        PERSIST("-no-snapshot-load"),
        NOT_PERSIST("-no-snapshot-save");

        private final String value;

        SNAPSHOT(String value) {
            this.value = value;
        }
    }

    public enum CameraFront {
        NONE("-camera-front", "emulator"),
        EMULATED("-camera-front", "none");

        private final String key;
        private final String value;
        CameraFront(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public enum CameraBack {
        NONE("-camera-back", "emulator"),
        EMULATED("-camera-back", "none");

        private final String key;
        private final String value;
        CameraBack(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    EmulatorManagerCLIBuilder(String sdkRoot) {
        this.sdkRoot = sdkRoot;
    }

    public EmulatorManagerCLIBuilder setEmulatorConfig(EmulatorConfig emulatorConfig) {
        this.emulatorConfig = emulatorConfig;
        return this;
    }

    public static EmulatorManagerCLIBuilder withSdkRoot(String sdkRoot) {
        return new EmulatorManagerCLIBuilder(sdkRoot);
    }

    public EmulatorManagerCLIBuilder setCameraFront(CameraFront cameraFront) {
        this.cameraFront = cameraFront;
        return this;
    }

    public EmulatorManagerCLIBuilder setCameraBack(CameraBack cameraBack) {
        this.cameraBack = cameraBack;
        return this;
    }

    public EmulatorManagerCLIBuilder setMode(SNAPSHOT mode) {
        this.mode = mode;
        return this;
    }

    public EmulatorManagerCLIBuilder setMemory(int memory) {
        this.memory = memory;
        return this;
    }

    public EmulatorManagerCLIBuilder setWipe(boolean wipe) {
        this.wipe = wipe;
        return this;
    }

    public EmulatorManagerCLIBuilder setConsolePort(int consolePort) {
        this.consolePort = consolePort;
        return this;
    }

    public EmulatorManagerCLIBuilder setAdbPort(Object adbPort) {
        this.adbPort = adbPort;
        return this;
    }

    public EmulatorManagerCLIBuilder setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
        return this;
    }

    public ChristelleCLICommand<Void> start() {
        if (emulatorConfig.getReportPort() < 5554) {
            throw new IllegalArgumentException("Emulator port must be greater or equals than 5554");
        }
        EnvVars env = new EnvVars();
        ArgumentListBuilder arguments = new ArgumentListBuilder();

        if (emulatorConfig.getEmulatorName() == null) {
            emulatorConfig.setEmulatorName(Constants.EMULATOR_DEFAULT_NAME);
        }
        arguments.add("-avd", emulatorConfig.getEmulatorName());

        if (emulatorConfig.getDataDir() != null) {
            arguments.add("-datadir");
            arguments.addQuoted(emulatorConfig.getDataDir());
        }

        arguments.add(mode.value);
        arguments.add(cameraFront.key, cameraFront.value);
        arguments.add(cameraBack.key, cameraBack.value);

        arguments.add(ARG_NO_AUDIO);
        env.put(Constants.ENV_VAR_QEMU_AUDIO_DRV, "none");

        // Disk Images and Memory params
        if (memory != -1) {
            arguments.add(ARG_MEMORY, String.valueOf(memory));
        }

        if (wipe) {
            arguments.add(ARG_WIPE_DATA);
        }

        if (emulatorConfig.getDeviceLocale() != null) {
            Locale l = Locale.forLanguageTag(emulatorConfig.getDeviceLocale());
            arguments.add(ARG_PROP, "persist.sys.language=" + l.getLanguage());
            arguments.add(ARG_PROP, "persist.sys.country=" + l.getCountry());
        }

        // Network params
        arguments.add(ARG_PORTS, consolePort + "," + adbPort);

        buildProxyArguments(arguments);

        // System params
        arguments.add(ARG_ACCEL, "auto");
        arguments.add(ARG_NO_WINDOW);

        // UI params
        arguments.add(ARG_NO_BOOT_ANIM);

        if (emulatorConfig.getReportPort() > 0) {
            arguments.add(ARG_REPORT_CONSOLE, "tcp:" + emulatorConfig.getReportPort() + ",max=" + emulatorConfig.getAdbConnectionTimeout());
        }

        return new ChristelleCLICommand<>(executable, arguments, env);

    }

    private void buildProxyArguments(ArgumentListBuilder arguments) {
        if (proxy == null) {
            return;
        }

        String userInfo = Util.fixEmptyAndTrim(proxy.getUserName());
        // append password only if userName is defined
        if (userInfo != null && StringUtils.isNotBlank(proxy.getEncryptedPassword())) {
            Secret secret = Secret.decrypt(proxy.getEncryptedPassword());
            if (secret != null) {
                userInfo += ":" + Util.fixEmptyAndTrim(secret.getPlainText());
            }
        }

        arguments.add(ARG_PROXY);
        try {
            String proxyURL = new URI("http", userInfo, proxy.name, proxy.port, null, null, null).toString();
            if (userInfo != null) {
                arguments.addMasked(proxyURL);
            } else {
                arguments.add(proxyURL);
            }
        } catch (URISyntaxException e) {
            if (userInfo != null) {
                arguments.addMasked(userInfo + "@" + proxy.name + ":" + proxy.port);
            } else {
                arguments.add(proxy.name + ":" + proxy.port);
            }
        }
    }

    public ChristelleCLICommand<Void> arguments(String[] args) {
        ArgumentListBuilder arguments = new ArgumentListBuilder();
        buildProxyArguments(arguments);
        arguments.add(args);

        return new ChristelleCLICommand<>(executable, arguments, new EnvVars());
    }

}
