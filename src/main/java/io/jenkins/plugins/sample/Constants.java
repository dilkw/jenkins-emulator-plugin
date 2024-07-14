package io.jenkins.plugins.sample;

public interface Constants {

    String EMULATOR_DEFAULT_NAME = "jenkins_emulator";
    String ANDROID_SDK_ROOT = "ANDROID_SDK_ROOT";

    String githubUrl = "https://github.com/jenkinsci/android-emulator-plugin.git\n";

    String BUILD_TOOLS_DIR = "build-tools";
    String EMULATOR_DIR = "emulator";
    String PLATFORM_TOOLS_DIR = "platform-tools";
    String PLATFORMS_DIR = "platforms";
    String TOOLS_DIR = "tools";
    String TOOLS_BIN_DIR = "tools/bin";
    String CMD_TOOLS_BIN_DIR = "cmdline-tools/bin";

    String ENV_ANDROID_SDK_HOME = "ANDROID_SDK_HOME";
    String ENV_ANDROID_AVD_HOME = "ANDROID_AVD_HOME";
    String ENV_ANDROID_ADB_HOME = "ANDROID_ADB_HOME";
    String ENV_ANDROID_EMULATOR_HOME = "ANDROID_EMULATOR_HOME";
    String ENV_VAR_QEMU_AUDIO_DRV = "QEMU_AUDIO_DRV";
    String ENV_VAR_ANDROID_SDK_ROOT = "ANDROID_SDK_ROOT";
    String ENV_VAR_PATH_SDK_TOOLS = "PATH+SDK_TOOLS";


    int ADB_DEFAULT_SERVER_PORT = 5037;


    String REGEX_SCREEN_RESOLUTION = "[0-9]{3,4}x[0-9]{3,4}";


    String ENV_ADB_TRACE = "ADB_TRACE";
    String ENV_ADB_LOCAL_TRANSPORT_MAX_PORT = "ADB_LOCAL_TRANSPORT_MAX_PORT";

    // android sdk
    String DDMS_CONFIG = "ddms.cfg";
    String LOCAL_REPO_CONFIG = "repositories.cfg";
    String ANDROID_CACHE = ".android";

}
