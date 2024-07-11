package io.jenkins.plugins.sample.cmd;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.model.Emulator;
import org.apache.commons.io.input.NullInputStream;

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

    private final FilePath command;
    private final EnvVars env;
    private final InputStream stdin = new NullInputStream(0);
    private final FilePath root;

    AVDManagerCLIBuilder(FilePath command, ArgumentListBuilder arguments, EnvVars env, FilePath root) {
        this.command = command;
        this.env = env;
        this.root = root;
    }

    // 创建 AVD, command: create avd -n name -k "sdk_id" [-c {path|size}] [-f] [-p path]
    public void createAVD() {

    }

    // 删除 AVD , command: delete avd -n name
    public void deleteAVD() {

    }

    // 获取已安装的 AVD 列表，command: list [target|device|avd] [-c]
    public List<Emulator> getEmulators() {
        return new ArrayList<>();
    }

    public ChristelleCLICommand<Void> build() {

        return new ChristelleCLICommand<>(command, getArguments(), env, root);
    }

    public ArgumentListBuilder getArguments() {
        ArgumentListBuilder builder = new ArgumentListBuilder();
        builder.addKeyValuePair("", ARG_SDK_ROOT, env.get(Constants.ANDROID_SDK_ROOT), false);
        return builder;
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
}

