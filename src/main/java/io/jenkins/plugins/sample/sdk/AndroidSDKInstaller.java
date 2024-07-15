package io.jenkins.plugins.sample.sdk;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolInstallation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.SDKManagerCLIBuilder;
import io.jenkins.plugins.sample.cmd.help.Channel;
import io.jenkins.plugins.sample.cmd.help.Platform;
import io.jenkins.plugins.sample.cmd.model.SDKPackages;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import io.jenkins.plugins.sample.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Automatic tools installer from google.
 *
 * @author Nikolas Falco
 *
 * @since 4.0
 */
public class AndroidSDKInstaller extends DownloadFromUrlInstaller {

    public class AndroidSDKInstallable extends NodeSpecificInstallable {

        @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
        public AndroidSDKInstallable(Installable inst) {
            super(inst);
        }

        @Override
        public NodeSpecificInstallable forNode(Node node, TaskListener log) throws IOException, InterruptedException {
            if (url == null) {
                throw new IllegalStateException("Installable " + name + " does not have a valid URL");
            }

            platform = Platform.of(node);
            String osName = platform.name().toLowerCase();
            switch (platform) {
            case WINDOWS:
                osName = id.startsWith("cmdline-tools") ? "win" : osName;
                break;
            default:
                // leave default
                break;
            }
            url = url.replace("{os}", osName);

            return this;
        }
        
    }

    private static final List<String> DEFAULT_PACKAGES = Arrays.asList("platform-tools", "build-tools;*", "emulator", "extras;android;m2repository", "extras;google;m2repository");

    private Platform platform;
    private final Channel channel;

    @DataBoundConstructor
    public AndroidSDKInstaller(String id, Channel channel) {
        super(id);
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public Installable getInstallable() throws IOException {
        Installable installable = super.getInstallable();
        if (installable == null) {
            return null;
        }
        return new AndroidSDKInstallable(installable);
    }

    @Override
    public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
        FilePath expected = super.performInstallation(tool, node, log);

        writeConfigurations(expected);
        installBasePackages(expected, log);
        return expected;
    }

    private void writeConfigurations(FilePath sdkRoot) throws IOException, InterruptedException {
        FilePath sdkHome = getSDKHome(sdkRoot);
        sdkHome.mkdirs();

        // configure DDMS
        FilePath ddmsConfig = sdkHome.child(Constants.DDMS_CONFIG);
        if (!ddmsConfig.exists()) {
            String settings = "pingOptIn=false\n";
            settings += "pingId=0\n";
            ddmsConfig.write(settings, StandardCharsets.UTF_8.name());
        }

        // configure for no local repositories
        FilePath localRepoCfg = sdkHome.child(Constants.LOCAL_REPO_CONFIG);
        if (!localRepoCfg.exists()) {
            localRepoCfg.write("count=0", StandardCharsets.UTF_8.name());
        }
    }

    private void installBasePackages(FilePath sdkRoot, TaskListener log) throws IOException, InterruptedException {
        FilePath sdkmanager = sdkRoot.child("tools").child("bin").child("sdkmanager" + platform.extension);
        if (!sdkmanager.exists()) {
            sdkmanager = sdkRoot.child("cmdline-tools").child("bin").child("sdkmanager" + platform.extension);
        }

        String remoteSDKRoot = sdkRoot.getRemote();
        String androidHome = getSDKHome(sdkRoot).getRemote();

        SDKPackages packages = SDKManagerCLIBuilder.withSDKRoot(sdkRoot.getRemote())
                .createExecutableFormPlatform(sdkRoot.createLauncher(log), platform)
                .setProxy(Jenkins.get().proxy)
                .setChannel(channel)
                .list()
                //.withEnv(Constants.ENV_VAR_ANDROID_SDK_HOME, androidHome) //
                .execute();

        // remove components already installed
        List<String> defaultPackages = DEFAULT_PACKAGES.stream() //
                .filter(defaultPackage -> packages.getInstalled().stream().noneMatch(i -> {
                    if (defaultPackage.endsWith("*")) {
                        String defPkg = StringUtils.removeEnd(defaultPackage, "*");
                        return i.getId().startsWith(defPkg);
                    }
                    return defaultPackage.equals(i.getId());
                })) //
                .collect(Collectors.toList());

        if (!defaultPackages.isEmpty()) {
            // get component with the available latest version
            List<String> components = new ArrayList<>();
            defaultPackages.forEach(defaultPackage -> components.add(packages.getAvailable().stream() //
                    // filter by component, the wildcards allow partial matching
                    .filter(p -> {
                        if (defaultPackage.endsWith("*")) {
                            String defPkg = StringUtils.removeEnd(defaultPackage, "*");
                            return p.getId().startsWith(defPkg);
                        }
                        return defaultPackage.equals(p.getId());
                    })
                    // remove release candidate versions for stable channel
                    .filter(p -> channel != Channel.STABLE || p.getVersion().getQualifier() == null) //
                    .sorted(Collections.reverseOrder()) // in case of wildcards we takes latest version
                    .findFirst().get().getId()));

            SDKManagerCLIBuilder.withSDKRoot(sdkRoot.getRemote()) //
                    .createExecutableFormPlatform(sdkRoot.createLauncher(log), platform)
                    .setProxy(Jenkins.get().proxy) //
                    .setChannel(channel) //
                    .addEnvVars(Constants.ENV_VAR_ANDROID_SDK_ROOT, androidHome)
                    .installSDK(components) //
                    .execute(log);
        }
    }

    @Override
    protected FilePath findPullUpDirectory(FilePath root) throws IOException, InterruptedException {
        // do not pull up, keep original structure
        return null;
    }

    private FilePath getSDKHome(FilePath sdkRoot) {
        return sdkRoot.child(Constants.ANDROID_CACHE);
    }

    @Extension
    public static final class DescriptorImpl extends DownloadFromUrlInstaller.DescriptorImpl<AndroidSDKInstaller> { // NOSONAR
        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
            return toolType == AndroidSDKInstallation.class;
        }

        @Override
        public String getDisplayName() {
            return Messages.AndroidSDKInstaller_displayName();
        }

        @NonNull
        @Override
        public List<? extends Installable> getInstallables() throws IOException {
            List<Installable> installables = Collections.emptyList();

            // latest available here https://developer.android.com/studio/index.html#command-tools
            try (InputStream is = getClass().getResourceAsStream("/" + getId() + ".json")) {
                if (is != null) {
                    String data = IOUtils.toString(is, StandardCharsets.UTF_8);
                    JSONObject json = JSONObject.fromObject(data);
                    installables = Arrays.asList(((InstallableList) JSONObject.toBean(json, InstallableList.class)).list);
                }
            }
            return installables;
        }

        public ListBoxModel doFillChannelItems(@QueryParameter String channel) {
            ListBoxModel channels = new ListBoxModel();
            for (Channel ch : Channel.values()) {
                channels.add(new Option(ch.getLabel(), ch.name(), ch.name().equals(channel)));
            }
            return channels;
        }
    }
}
