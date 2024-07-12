package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.Channel;
import io.jenkins.plugins.sample.cmd.help.Platform;
import io.jenkins.plugins.sample.cmd.help.ToolsCommand;
import io.jenkins.plugins.sample.cmd.help.Utils;
import io.jenkins.plugins.sample.cmd.model.SDKPackages;
import io.jenkins.plugins.sample.cmd.model.Version;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SDKManagerCLIBuilder implements Cloneable {


    private ArgumentListBuilder arguments;
    private FilePath executable;;
    private EnvVars env;
    private Channel channel = Channel.STABLE;
    private String sdkRoot;
    private ProxyConfiguration proxy;

    List<ChristelleCLICommand<Object>> christelleCLICommandList = new ArrayList<>();

    private static final String NO_PREFIX = "";
    private static final String ARG_OBSOLETE = "--include_obsolete";
    private static final String ARG_VERBOSE = "--verbose";
    private static final String ARG_CHANNEL = "--channel";
    private static final String ARG_SDK_ROOT = "--sdk_root";
    private static final String ARG_INSTALL = "--install";
    private static final String ARG_UPDATE = "--update";
    private static final String ARG_LIST = "--list";
    private static final String ARG_PROXY_HOST = "--proxy_host";
    private static final String ARG_PROXY_PORT = "--proxy_port";
    private static final String ARG_PROXY_PROTOCOL = "--proxy";

    private SDKManagerCLIBuilder(String sdkRoot) {
        this.sdkRoot = sdkRoot;
    }

    public static SDKManagerCLIBuilder withSDKRoot(String sdkRoot) {
        return new SDKManagerCLIBuilder(sdkRoot);
    }

    public SDKManagerCLIBuilder command(@NonNull final FilePath executable) {
        this.executable = executable;
        return this;
    }

    public SDKManagerCLIBuilder addArgument(@NonNull String arg) {
        this.arguments.add(arg);
        return this;
    }

    public SDKManagerCLIBuilder setChannel(@NonNull final Channel channel) {
        this.channel = channel;
        return this;
    }

    public SDKManagerCLIBuilder setProxy(ProxyConfiguration proxy) {
        this.proxy = proxy;
        return this;
    }

    public ChristelleCLICommand<Void> installSDK(@NonNull Collection<String> packages) {
        // --install "platforms;android-30" "system-images;android-30;default;x86_64
        //arguments.add(" --install \"platforms;" + androidAPIVersion + " \"system-images;" + androidAPIVersion + ";default;" + ABI);

        return new ChristelleCLICommand<>(this.executable, this.arguments, this.env);

    }

    public SDKManagerCLIBuilder addEnvironment(@NonNull EnvVars env) {
        if (this.env == null) {
            this.env = new EnvVars();
        }
        this.env.putAll(env);
        return this;
    }

    public SDKManagerCLIBuilder addEnvironment(@NonNull String key, @NonNull String value) {
        if (this.env == null) {
            this.env = new EnvVars();
        }
        this.env.put(key, value);
        return this;
    }

    public SDKManagerCLIBuilder createExecutable(final Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        String toolRoot = sdkRoot + Constants.CMD_TOOLS_BIN_DIR;
        executable = Utils.createExecutable(launcher, workspace, toolRoot);
        return this;
    }

    public ChristelleCLICommand<Void> buildCommand() {
        return new ChristelleCLICommand<>(executable, arguments, env);
    }

    public ChristelleCLICommand<SDKPackages> list() {
        ListPackagesParser parser = new ListPackagesParser();
        ChristelleCLICommand<SDKPackages> christelleCLICommand = new ChristelleCLICommand<>(executable, arguments, env);
        return christelleCLICommand.withParser(parser);
    }

    public ChristelleCLICommand<Void> installSDK() throws Exception {
        return new ChristelleCLICommand<>(executable, arguments, env);
    }

    @Override
    public SDKManagerCLIBuilder clone() {
        try {
            return (SDKManagerCLIBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    private enum Column {
        NAME, VERSION, LOCATION, AVAILABLE, DESCRIPTION, UNSUPPORTED
    }

    static class ListPackagesParser implements ChristelleCLICommand.OutputParser<SDKPackages> {
        @Override
        public SDKPackages parse(InputStream input) throws IOException {
            SDKPackages result = new SDKPackages();

            List<Column> columns = null;
            List<SDKPackages.SDKPackage> bucket = null;
            for (String line : IOUtils.readLines(input, "UTF-8")) { // NOSONAR
                line = Util.fixEmptyAndTrim(line);
                if (StringUtils.isBlank(line)) {
                    continue;
                }

                String lcLine = line.toLowerCase();
                if (StringUtils.isBlank(line)) {
                    continue;
                } else if (lcLine.startsWith("available packages")) {
                    bucket = result.getAvailable();
                    continue;
                } else if (lcLine.startsWith("installed packages")) {
                    bucket = result.getInstalled();
                    continue;
                } else if (lcLine.startsWith("available updates")) {
                    bucket = result.getUpdates();
                    continue;
                } else if (bucket == null || lcLine.startsWith("--")) {
                    continue;
                } else if (isHeader(lcLine)) {
                    columns = createMapping(lcLine);
                    continue;
                }

                // finally it's a table row
                SDKPackages.SDKPackage sdkPackage = new SDKPackages.SDKPackage();

                StringTokenizer st = new StringTokenizer(line, "|");
                if (columns == null || columns.isEmpty()){
                    continue;
                }
                for (Column column : columns) { // NOSONAR
                    if (!st.hasMoreTokens()) {
                        // guard in case cells are empty
                        continue;
                    }

                    String value = Util.fixEmptyAndTrim(st.nextToken());
                    if (value == null) {
                        continue;
                    }

                    switch (column) {
                        case NAME:
                            sdkPackage.setId(value);
                            break;
                        case DESCRIPTION:
                        case LOCATION:
                            sdkPackage.setDescription(value);
                            break;
                        case VERSION:
                        case AVAILABLE:
                            sdkPackage.setVersion(new Version(value));
                            break;
                        case UNSUPPORTED:
                            // skip
                            break;
                    }
                }

                bucket.add(sdkPackage);
            }
            return result;
        }

        private List<Column> createMapping(String headers) {
            List<Column> columns = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(headers, "|");
            while (st.hasMoreTokens()) {
                switch (st.nextToken().trim()) {
                    case "path":
                    case "id":
                        columns.add(Column.NAME);
                        break;
                    case "version":
                    case "installed":
                        columns.add(Column.VERSION);
                        break;
                    case "description":
                        columns.add(Column.DESCRIPTION);
                        break;
                    case "location":
                        columns.add(Column.LOCATION);
                        break;
                    case "available":
                        columns.add(Column.AVAILABLE);
                        break;
                    default:
                        // unsupported
                        break;
                }
            }
            return columns;
        }

        private boolean isHeader(String line) {
            return line.startsWith("id") || line.startsWith("path");
        }
    }
}

