package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.Platform;
import io.jenkins.plugins.sample.cmd.help.ToolsCommand;
import io.jenkins.plugins.sample.cmd.help.Utils;
import io.jenkins.plugins.sample.cmd.model.AVDevice;
import io.jenkins.plugins.sample.cmd.model.EmulatorConfig;
import io.jenkins.plugins.sample.cmd.model.Targets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AVDManagerCLIBuilder implements Cloneable {

    // AVD 路径 eg: --sdk_root=/var/android-sdk
    private final String ARG_SDK_ROOT = "--sdk_root";
    // AVD 名称参数 eg: -n test
    private final String ARG_AVD_NAME = "-n";
    // AVD sdk 包 id eg: -k "system-images;android-25;google_apis;x86"
    private final String ARG_AVD_SDK_ID = "-k";
    // AVD sd-card 映射路径 eg: -c path/to/sdcard/ 或 -c 1000M 或 -c path/to/sdcard/ | -c 1000M
    private final String ARG_AVD_SDCARD_MAPPING = "-c";
    // AVD 的文件的目录所在位置的路径，未指定路径，则系统会在 ~/.android/avd/ 中创建 AVD，eg: -p /var
    private final String ARG_AVD_FILE_ROOT = "-p";

    // 设置为静默模拟，不会在控制台打印
    private static final String ARG_SILENT = "--silent";
    // 设置为详细模式，会在控制台打印详细信息
    private static final String ARG_VERBOSE = "--verbose";
    // 清除 avdmanager 工具的缓存
    private static final String ARG_CLEAR_CACHE = "--clear-cache";
    private static final String[] ARG_LIST_TARGET = new String[] { "list", "target" };
    private static final String[] ARG_LIST_AVD = new String[] { "list", "avd" };
    private static final String[] ARG_CREATE = new String[] { "create", "avd" };
    private static final String[] ARG_DELETE = new String[] { "delete", "avd" };
    private static final String ARG_NAME = "--name";
    private static final String ARG_PACKAGE = "--package";
    private static final String ARG_FORCE = "--force";
    private static final String ARG_DEVICE = "--device";
    private static final String ARG_ABI = "--abi";
    private static final String ARG_SDCARD = "--sdcard";

    private FilePath executable;
    private EnvVars env;
    private final String sdkRoot;
    private boolean silent;
    boolean verbose;
    private String packagePath;
    private String device;
    private String sdkCardRoot;


    public AVDManagerCLIBuilder(String sdkRoot) {
        this.sdkRoot = sdkRoot;
    }

    public static AVDManagerCLIBuilder withSdkRoot(String sdkRoot) {
        return new AVDManagerCLIBuilder(sdkRoot);
    }

    public AVDManagerCLIBuilder device(String device) {
        this.device = device;
        return this;
    }

    public AVDManagerCLIBuilder createExecutable(final Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        executable = Utils.createExecutable(launcher, workspace, sdkRoot, ToolsCommand.AVD_MANAGER);
        return this;
    }

    // 创建 AVD, command: create avd -n name -k "sdk_id" [-c {path|size}] [-f] [-p path]
    public ChristelleCLICommand<Void> createAVD(EmulatorConfig emulatorConfig) {
        if (emulatorConfig.getEmulatorName() == null || emulatorConfig.getEmulatorName().isEmpty()) {
            throw new IllegalArgumentException("Device name is required");
        }

        ArgumentListBuilder arguments = new ArgumentListBuilder();

        addGlobalOptions(arguments);

        // action
        arguments.add(ARG_CREATE);

        // action options
        arguments.add(ARG_NAME, emulatorConfig.getEmulatorName());

        if (packagePath != null) {
            arguments.add(ARG_PACKAGE, packagePath);
        }
        if (device != null) {
            arguments.add(ARG_DEVICE, device);
        }
        if (emulatorConfig.getTargetABI() != null) {
            arguments.add(ARG_ABI, emulatorConfig.getTargetABI());
        }
        if (sdkCardRoot != null && !sdkCardRoot.isEmpty()) {
            arguments.add(ARG_SDCARD, sdkCardRoot);
        }
        arguments.add(ARG_FORCE);

        EnvVars env = new EnvVars();
        additionalEnv(env);

        return new ChristelleCLICommand<Void>(executable, arguments, env)
                .withInput("\r\n");
    }

    // 删除 AVD , command: delete avd -n name
    public ChristelleCLICommand<Void> deleteAVD(String emulatorName) {
        if (emulatorName == null || emulatorName.isEmpty()) {
            throw new IllegalArgumentException("Device name is required");
        }

        ArgumentListBuilder arguments = new ArgumentListBuilder();

        addGlobalOptions(arguments);

        // action
        arguments.add(ARG_DELETE);

        // action options
        arguments.add(ARG_NAME, emulatorName);

        EnvVars env = new EnvVars();
        additionalEnv(env);

        return new ChristelleCLICommand<>(executable, arguments, env);
    }

    // 获取已安装的 AVD 列表，command: list [target|device|avd] [-c]
    public List<EmulatorConfig> getEmulators() {
        return new ArrayList<>();
    }

    public AVDManagerCLIBuilder packagePath(String packagePath) {
        this.packagePath = packagePath;
        return this;
    }

    public ArgumentListBuilder getArguments() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.addKeyValuePair("", ARG_SDK_ROOT, env.get(Constants.ANDROID_SDK_ROOT), false);
        return builder;
    }

    public AVDManagerCLIBuilder addEnv(EnvVars env) {
        if (this.env == null) {
            this.env = new EnvVars();
        }
        this.env.putAll(env);
        return this;
    }

    public AVDManagerCLIBuilder silent(boolean silent) {
        this.silent = silent;
        return this;
    }

    public AVDManagerCLIBuilder setSdkCardRoot(String sdkCardRoot) {
        this.sdkCardRoot = sdkCardRoot;
        return this;
    }

    public ChristelleCLICommand<List<AVDevice>> listAVD() {
        ArgumentListBuilder arguments = new ArgumentListBuilder();

        addGlobalOptions(arguments);

        // action
        arguments.add(ARG_LIST_AVD);

        EnvVars env = new EnvVars();
        additionalEnv(env);

        return new ChristelleCLICommand<List<AVDevice>>(executable, arguments, env) //
                .withParser(new ListAVDParser());
    }

    public ChristelleCLICommand<Void> build() {
        return new ChristelleCLICommand<>(executable, getArguments(), env);
    }

    private void addGlobalOptions(ArgumentListBuilder arguments) {
        if (verbose) {
            arguments.add(ARG_VERBOSE);
        } else if (silent) {
            arguments.add(ARG_SILENT);
        }

        arguments.add(ARG_CLEAR_CACHE);
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void additionalEnv(EnvVars env) {
        // fix a bug in windows script where calculates wrong the
        // SDK root because raising up two parent instead of one
        env.put("AVDMANAGER_OPTS", "-Dcom.android.sdkmanager.toolsdir=" + executable.getParent().getRemote());
    }

    public void startAVD() {

    }

    @Override
    public AVDManagerCLIBuilder clone() {
        try {
            return (AVDManagerCLIBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    static class ListTargetParser implements ChristelleCLICommand.OutputParser<List<Targets>> {

        @Override
        public List<Targets> parse(InputStream input) throws IOException {
            List<Targets> targets = new ArrayList<>();

            boolean context = false; // indicates when the useful text starting
            // for parsing
            Targets target = null;
            for (String line : IOUtils.readLines(input, "UTF-8")) { // NOSONAR
                line = Util.fixEmptyAndTrim(line);
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                String lcLine = line.toLowerCase();
                if (!context || isHeader(lcLine)) {
                    context |= lcLine.startsWith("available android targets");
                    continue;
                }

                String key = lcLine.split(":")[0];
                String value = Util.fixEmptyAndTrim(line.split(":")[1]);
                if (value != null) {
                    switch (key) {
                        case "id":
                            target = new Targets();
                            targets.add(target);
                            int idx = value.indexOf('"');
                            target.setId(value.substring(idx + 1, value.lastIndexOf('"')));
                            break;
                        case "name":
                            if (target != null) {
                                target.setName(value);
                            }
                            break;
                        case "type":
                            if (target != null) {
                                target.setType(Targets.TargetType.valueOf(value.toLowerCase()));
                            }
                            break;
                        case "api level":
                            if (target != null) {
                                target.setAPILevel(Integer.parseInt(value));
                            }
                            break;
                        case "revision":
                            if (target != null) {
                                target.setRevision(Integer.parseInt(value));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            return targets;
        }

        private boolean isHeader(String lcLine) {
            return lcLine.startsWith("-") || lcLine.contains("loading local repository");
        }

    }

    static class ListAVDParser implements ChristelleCLICommand.OutputParser<List<AVDevice>> {

        @Override
        public List<AVDevice> parse(InputStream input) throws IOException {
            List<AVDevice> devices = new ArrayList<>();

            boolean context = false; // indicates when the useful text starting
            // for parsing
            AVDevice device = null;
            for (String line : IOUtils.readLines(input, "UTF-8")) { // NOSONAR
                line = Util.fixEmptyAndTrim(line);
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                String lcLine = line.toLowerCase();
                if (!context || isHeader(lcLine) || lcLine.contains("android virtual devices could not be loaded")) {
                    context |= lcLine.startsWith("available android virtual devices");
                    continue;
                }

                String key = getKey(lcLine);
                String value = getValue(line);
                if (value != null) {
                    switch (key) {
                        case "name":
                            device = new AVDevice();
                            device.setName(value);
                            devices.add(device);
                            break;
                        case "path":
                            if (device != null) {
                                device.setPath(value);
                            }
                            break;
                        case "target":
                            if (device != null) {
                                device.setTarget(value);
                            }
                            break;
                        case "based on":
                            if (device != null) {
                                device.setAndroidOS(value);
                            }
                            break;
                        case "tag/abi":
                            if (device != null) {
                                device.setAndroidOS(value);
                            }
                            break;
                        case "sdcard":
                            if (device != null) {
                                device.setSDCard(value);
                            }
                            break;
                        case "error":
                            if (device != null) {
                                device.setError(value);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            return devices;
        }

        private String getValue(String line) {
            String[] split = line.split(":");
            if (split.length > 1) {
                return Util.fixEmptyAndTrim(split[1]);
            } else {
                return null;
            }
        }

        private String getKey(String lcLine) {
            return lcLine.split(":")[0];
        }

        private boolean isHeader(String lcLine) {
            return lcLine.startsWith("-") || lcLine.contains("loading local repository");
        }
    }
}

