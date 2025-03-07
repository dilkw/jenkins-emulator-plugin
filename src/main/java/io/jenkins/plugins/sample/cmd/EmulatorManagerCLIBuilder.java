package io.jenkins.plugins.sample.cmd;

import hudson.*;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.ToolsCommand;
import io.jenkins.plugins.sample.cmd.help.Utils;
import io.jenkins.plugins.sample.cmd.model.EmulatorConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
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
    private int memory = -1;
    private boolean wipe;
    private ProxyConfiguration proxy;
    private String dataDir;

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

    public enum LibFile {
        LIB_X11("libX11.so.6", "libx11-6");
        private final String key;
        private final String value;
        LibFile(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    EmulatorManagerCLIBuilder(String sdkRoot) {
        this.sdkRoot = sdkRoot;
    }

    public EmulatorManagerCLIBuilder createExecutable(final Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        executable = Utils.createExecutable(launcher, workspace, sdkRoot, ToolsCommand.EMULATOR);
        return this;
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

    public EmulatorManagerCLIBuilder setEmulatorConsolePort(int consolePort) {
        emulatorConfig.setEmulatorConsolePort(consolePort);
        return this;
    }

    public EmulatorManagerCLIBuilder setEmulatorADBConnectPort(int adbPort) {
        emulatorConfig.setEmulatorADBConnectPort(adbPort);
        return this;
    }

    public EmulatorManagerCLIBuilder setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
        return this;
    }

    public EmulatorManagerCLIBuilder setDataDir(String dataDir) {
        this.dataDir = dataDir;
        return this;
    }

    public ChristelleCLICommand<Void> start(TaskListener listener) throws IOException, InterruptedException {
        if (emulatorConfig.getEmulatorConsolePort() < 5554) {
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
            arguments.addQuoted(dataDir);
        }

        arguments.add(mode.value);
        if (cameraFront != null) {
            arguments.add(cameraFront.key, cameraFront.value);
        }

        if (cameraBack != null) {
            arguments.add(cameraBack.key, cameraBack.value);
        }


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
        arguments.add(ARG_PORTS, emulatorConfig.getEmulatorConsolePort() + "," + emulatorConfig.getEmulatorADBConnectPort());

        buildProxyArguments(arguments);

        // System params
        arguments.add(ARG_ACCEL, "auto");
        arguments.add(ARG_NO_WINDOW);

        // UI params
        arguments.add(ARG_NO_BOOT_ANIM);

        if (emulatorConfig.getEmulatorReportConsolePort() > 0) {
            arguments.add(ARG_REPORT_CONSOLE, "tcp:" + emulatorConfig.getEmulatorReportConsolePort() + ",max=" + emulatorConfig.getEmulatorConnectToAdbTimeout());
        }

        env.replace(Constants.ENV_VAR_ANDROID_SDK_ROOT, "E:\\command-line-android-sdk");

        LibNotFoundParser libNotFoundParser = new LibNotFoundParser(executable.createLauncher(listener));
        return new ChristelleCLICommand<Void>(executable, arguments, env)
                .withParser(libNotFoundParser);
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

    ///var/jenkins_home/android-sdk/emulator/emulator: error while loading shared libraries: libX11.so.6: cannot open shared object file: No such file or directory
    static class LibNotFoundParser implements ChristelleCLICommand.OutputParser<Void>  {

        Launcher launcher;

        public LibNotFoundParser(Launcher launcher) {
            this.launcher = launcher;
        }

        @Override
        public Void parse(InputStream input) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : IOUtils.readLines(input, "UTF-8")) {
                stringBuilder.append(line).append("\r\n");
            }
            String result = stringBuilder.toString();
            if (result.contains(LibFile.LIB_X11.key)) {
                try {
                    String argument = "apt-get update && apt-get install -y " + LibFile.LIB_X11.value;
                    ChristelleCLICommand.executeWithArgument(launcher, argument, null);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }

}
