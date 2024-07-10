package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.SDKPackages;
import io.jenkins.plugins.sample.cmd.help.Version;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class SDKManagerCLIBuilder implements Cloneable {


    private String sdkRoot = "";
    private ArgumentListBuilder arguments;
    private FilePath command;
    private Map<String, String> env;
    private FilePath root;

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

    public SDKManagerCLIBuilder() {
        this.arguments = new ArgumentListBuilder();
        this.arguments.add(Constants.SDK_MANAGER);
    }

    public SDKManagerCLIBuilder command(@NonNull final FilePath command) {
        this.command = command;
        return this;
    }

    public SDKManagerCLIBuilder addArgument(@NonNull String arg) {
        this.arguments.add(arg);
        return this;
    }

    public SDKManagerCLIBuilder setSdkRoot(@NonNull final String sdkRoot) {
        this.sdkRoot = sdkRoot;
        return this;
    }

    public SDKManagerCLIBuilder installSDK(@NonNull final String androidAPIVersion, @NonNull final String ABI) {
        // --install "platforms;android-30" "system-images;android-30;default;x86_64
        arguments.add(" --install \"platforms;" + androidAPIVersion + " \"system-images;" + androidAPIVersion + ";default;" + ABI);
        return this;
    }

    public SDKManagerCLIBuilder addEnvironment(@NonNull EnvVars env) {
        this.env.putAll(env);
        return this;
    }

    public SDKManagerCLIBuilder addEnvironment(@NonNull String key, @NonNull String value) {
        this.env.put(key, value);
        return this;
    }

    public SDKManagerCLIBuilder root(@NonNull final FilePath root) {
        this.root = root;

        return this;
    }

    public CLICommand<Void> buildCommand() {
        return new CLICommand<Void>(command, arguments, env, root);
    }

    @Override
    public SDKManagerCLIBuilder clone() {
        try {
            SDKManagerCLIBuilder clone = (SDKManagerCLIBuilder) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    private enum Column {
        NAME, VERSION, LOCATION, AVAILABLE, DESCRIPTION, UNSUPPORTED
    }

    static class ListPackagesParser implements CLICommand.OutputParser<SDKPackages> {
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

