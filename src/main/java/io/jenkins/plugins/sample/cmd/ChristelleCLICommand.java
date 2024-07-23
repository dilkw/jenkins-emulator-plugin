package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.ForkOutputStream;
import hudson.util.StreamTaskListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.jenkins.plugins.sample.Constants;
import org.apache.commons.io.input.NullInputStream;
import org.apache.tools.ant.filters.StringInputStream;

public class ChristelleCLICommand<R> {
    private final ArgumentListBuilder arguments;
    private final FilePath command;
    private final EnvVars env;
    private InputStream stdin = new NullInputStream(0);
    private FilePath root;
    private OutputParser<R> parser;

    public ArgumentListBuilder getArguments() {
        return arguments;
    }

    ChristelleCLICommand(FilePath command, ArgumentListBuilder arguments, EnvVars env) {
        this.command = command;
        this.arguments = arguments;
        this.env = env;
    }

    public ChristelleCLICommand<R> withEnv(EnvVars env) {
        this.env.putAll(env);
        return this;
    }
    public ChristelleCLICommand<R> withEnv(String key, String value) {
        env.put(key, value);
        return this;
    }

    public static void executeWithArgument(Launcher launcher, String argument, TaskListener listener) throws IOException, InterruptedException {
        Launcher.ProcStarter starter = launcher.launch();
        if (listener != null) {
            starter.stdout(listener);
        }
        starter.cmds(argument);
        starter.start();
    }

    public R execute(Launcher launcher) throws IOException, InterruptedException {
        TaskListener listener = new StreamTaskListener(OutputStream.nullOutputStream(), StandardCharsets.UTF_8);
        return execute(listener, launcher);
    }

    public R execute() throws IOException, InterruptedException {
        return execute(new StreamTaskListener(OutputStream.nullOutputStream(), StandardCharsets.UTF_8), null);
    }

    public R execute(@NonNull TaskListener output) throws IOException, InterruptedException {
        return execute(output, null);
    }

    public R execute(@NonNull TaskListener output, Launcher launcher) throws IOException, InterruptedException {
        Launcher.ProcStarter starter = buildCommand(output, launcher);
        starter.stdout(output);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (parser != null) {
            starter.stdout(new ForkOutputStream(output.getLogger(), baos));
        } else {
            starter.stdout(output);
        }

        System.out.println("starter cmds" + starter.cmds());
        int exitCode = starter.join();
        if (exitCode != 0) {
            throw new IOException(command.getBaseName() + " " + arguments.toString() + " failed. exit code: " + exitCode + ".");
        }

        if (parser != null) {
            return parser.parse(new ByteArrayInputStream(baos.toByteArray()));
        }
        return null;
    }

    public Proc executeAsync(@Nullable TaskListener output) throws IOException, InterruptedException {
        return executeAsync(output, null);
    }

    public Proc executeAsync(@Nullable TaskListener output, Launcher launcher) throws IOException, InterruptedException {
        Launcher.ProcStarter starter = buildCommand(output, launcher);
        if (output != null) {
            starter.stdout(output);
        }
        return starter.start();
    }

    public R executeAsyncReturnData(@NonNull TaskListener output, Launcher launcher) throws IOException, InterruptedException {
        Launcher.ProcStarter starter = buildCommand(output, launcher);
        starter.stdout(output.getLogger());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (parser != null) {
            starter.stdout(stream);
        }
        Proc proc = starter.start();
        // 等待进程完成
        int exitCode = proc.joinWithTimeout(Constants.BOOT_COMPLETE_TIMEOUT_MS, TimeUnit.MILLISECONDS, output);
        if (exitCode != 0) {
            throw new IOException(command.getBaseName() + " " + arguments.toString() + " failed. exit code: " + exitCode);
        }
        if (parser != null) {
            return parser.parse(new ByteArrayInputStream(stream.toByteArray()));
        }
        return null;
    }

    private Launcher.ProcStarter buildCommand(@Nullable TaskListener output, Launcher launcher) throws IOException, InterruptedException {
        List<String> args = getArgumentsToList();
        StringBuilder stringBuilder = new StringBuilder("Command:");
        for (String arg : args) {
            stringBuilder.append(" ").append(arg);
        }
        System.out.println(stringBuilder);
        if (output != null) {
            output.getLogger().println(stringBuilder);
        }
        if (launcher == null) {
            launcher = command.createLauncher(output);
        }
        return launcher.launch()
                .envs(env)
                .stdin(stdin)
                .pwd(root == null ? command.getParent() : root)
                .cmds(args)
                .masks(getMasks(args.size()));
    }

    private boolean[] getMasks(final int size) {
        boolean[] masks = new boolean[size];
        masks[0] = false;
        System.arraycopy(arguments.toMaskArray(), 0, masks, 1, size - 1);
        return masks;
    }

    private List<String> getArgumentsToList() {
        List<String> args = new ArrayList<>(arguments.toList());
        args.add(0, command.getRemote());
        return args;
    }

    private String getCommand() {
        List<String> args = getArgumentsToList();
        StringBuilder stringBuilder = new StringBuilder("Command: ");
        for (String argument : args) {
            stringBuilder.append(argument);
        }
        return stringBuilder.toString();
    }

    public ChristelleCLICommand<R> withParser(OutputParser<R> parser) {
        this.parser = parser;
        return this;
    }

    public ChristelleCLICommand<R> setRoot(FilePath root) {
        this.root = root;
        return this;
    }

    ChristelleCLICommand<R> withInput(String input) {
        this.stdin = new StringInputStream(input);
        return this;
    }

    public interface OutputParser<R> {
        R parse(InputStream input) throws IOException;
    }

}
