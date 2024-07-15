package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.util.ArgumentListBuilder;
import hudson.util.Secret;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public class SDKManagerCLIBuilder implements Cloneable {


    private ArgumentListBuilder arguments;
    private FilePath executable;;
    private EnvVars env;
    private Channel channel = Channel.STABLE;
    private String sdkRoot;
    private ProxyConfiguration proxy;
    private boolean verbose;
    private boolean obsolete;

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

    public SDKManagerCLIBuilder setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public SDKManagerCLIBuilder setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
        return this;
    }

    public ChristelleCLICommand<Void> installSDK(@NonNull Collection<String> packages) {
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("At least a packge must be specified");
        }

        ArgumentListBuilder arguments = buildCommonOptions();

        arguments.add(ARG_INSTALL);
        for (String p : packages) {
            arguments.addQuoted(p);
        }

        EnvVars env = new EnvVars();
        try {
            buildProxyEnvVars(env);
        } catch (URISyntaxException e) {
            // fallback to CLI arguments
            buildProxyArguments(arguments);
        }
        return new ChristelleCLICommand<Void>(executable, arguments, env)
                .withInput(
                        StringUtils.repeat(
                                "y",
                                "\r\n",
                                packages.size()
                        )
                );

    }

    public SDKManagerCLIBuilder addEnvVars(@NonNull EnvVars env) {
        if (this.env == null) {
            this.env = new EnvVars();
        }
        this.env.putAll(env);
        return this;
    }

    public SDKManagerCLIBuilder addEnvVars(@NonNull String key, @NonNull String value) {
        if (this.env == null) {
            this.env = new EnvVars();
        }
        this.env.put(key, value);
        return this;
    }

    public SDKManagerCLIBuilder createExecutable(final Launcher launcher, FilePath workspace) throws InterruptedException, IOException {
        this.executable = Utils.createExecutable(launcher, workspace, sdkRoot, ToolsCommand.SDK_MANAGER);
        return this;
    }

    public SDKManagerCLIBuilder createExecutableFormPlatform(final Launcher launcher, Platform platform) throws InterruptedException, IOException {
        this.executable = Utils.createExecutable(launcher, platform, sdkRoot, ToolsCommand.SDK_MANAGER);
        return this;
    }

    public ChristelleCLICommand<Void> buildCommand() {
        return new ChristelleCLICommand<>(executable, arguments, env);
    }

    public ChristelleCLICommand<SDKPackages> list() {

        ArgumentListBuilder arguments = buildCommonOptions();
        arguments.add(ARG_LIST);

        EnvVars env = new EnvVars();
        try {
            buildProxyEnvVars(env);
        } catch (URISyntaxException e) {
            // fallback to CLI arguments
            buildProxyArguments(arguments);
        }
        ListPackagesParser parser = new ListPackagesParser();
        System.out.println("executable----" + executable.getRemote());
        ChristelleCLICommand<SDKPackages> christelleCLICommand = new ChristelleCLICommand<>(executable, arguments, env);
        return christelleCLICommand.withParser(parser);
    }

    private ArgumentListBuilder buildCommonOptions() {
        ArgumentListBuilder arguments = new ArgumentListBuilder();
        if (sdkRoot == null) {
            sdkRoot = getSDKRoot();
        }
        // required
        arguments.addKeyValuePair(NO_PREFIX, ARG_SDK_ROOT, quote(sdkRoot), false);

        if (channel != null) {
            arguments.addKeyValuePair(NO_PREFIX, ARG_CHANNEL, String.valueOf(channel.getValue()), false);
        }

        if (verbose) {
            arguments.add(ARG_VERBOSE);
        }

        if (obsolete) {
            arguments.add(ARG_OBSOLETE);
        }
        // arguments.add(ARG_FORCE_HTTP);

        return arguments;
    }

    private void buildProxyEnvVars(EnvVars env) throws URISyntaxException {
        if (proxy == null) {
            // no proxy configured
            return;
        }

        for (Pattern proxyPattern : proxy.getNoProxyHostPatterns()) {
            if (proxyPattern.matcher("https://dl.google.com/android/repository").find()) {
                // no proxy for google download repositories
                return;
            }
        }

        String userInfo = Util.fixEmptyAndTrim(proxy.getUserName());
        // append password only if userName is defined
        if (userInfo != null && StringUtils.isNotBlank(proxy.getSecretPassword().getEncryptedValue())) {
            Secret secret = Secret.decrypt(proxy.getSecretPassword().getEncryptedValue());
            if (secret != null) {
                userInfo += ":" + Util.fixEmptyAndTrim(secret.getPlainText());
            }
        }

        // ENV variables are used by
        // com.android.sdklib.tool.sdkmanager.SdkManagerCliSettings
        // actually authentication is not supported by the build tools !!!
        String proxyURL = new URI("http", userInfo, proxy.name, proxy.port, null, null, null).toString();
        env.put("HTTP_PROXY", proxyURL);
        env.put("HTTPS_PROXY", proxyURL);
    }

    private void buildProxyArguments(ArgumentListBuilder arguments) {
        if (proxy == null) {
            // no proxy configured
            return;
        }

        for (Pattern proxyPattern : proxy.getNoProxyHostPatterns()) {
            if (proxyPattern.matcher("https://dl.google.com/android/repository").find()) {
                // no proxy for google download repositories
                return;
            }
        }

        arguments.addKeyValuePair(NO_PREFIX, ARG_PROXY_PROTOCOL, "http", false);
        arguments.addKeyValuePair(NO_PREFIX, ARG_PROXY_HOST, proxy.name, false);
        if (proxy.port != -1) {
            arguments.addKeyValuePair(NO_PREFIX, ARG_PROXY_PORT, String.valueOf(proxy.port), false);
        }
    }

    private String getSDKRoot() {
        return executable.getParent().getParent().getParent().getRemote();
    }

    private String quote(String quote) {
        if (!StringUtils.isNotBlank(quote)) {
            return "\"" + quote + "\"";
        }
        return quote;
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

