package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import org.apache.commons.io.input.NullInputStream;
import org.apache.tools.ant.filters.StringInputStream;

public class ChristelleCLICommand<R> {
    private final ArgumentListBuilder arguments;
    private final FilePath command;
    private final Map<String, String> env;
    private InputStream stdin = new NullInputStream(0);
    private FilePath root;
    private OutputParser<R> parser;

    public ArgumentListBuilder getArguments() {
        return arguments;
    }

    ChristelleCLICommand(FilePath command, ArgumentListBuilder arguments, Map<String, String> env) {
        this.command = command;
        this.arguments = arguments;
        this.env = env;
    }

    public R execute() throws IOException, InterruptedException {
        return execute(new StreamTaskListener(OutputStream.nullOutputStream(), StandardCharsets.UTF_8));
    }

    public R execute(@NonNull TaskListener output) throws IOException, InterruptedException {
        Launcher.ProcStarter starter = buildCommand(output);
        starter.stdout(output);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (parser != null) {
            starter.stdout(new ForkOutputStream(output.getLogger(), baos));
        } else {
            starter.stdout(output);
        }

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
        // command.createLauncher(output)

        Launcher.ProcStarter starter = buildCommand(output);

        if (output != null) {
            starter.stdout(output);
        }

        return starter.start();
    }

    private Launcher.ProcStarter buildCommand(@Nullable TaskListener output) throws IOException, InterruptedException {
        System.out.println("command = " + command.getRemote());
        List<String> args = getArgumentsToList();
        return command.createLauncher(output).launch() //
                .envs(env) //
                .stdin(stdin) //
                .pwd(root == null ? command.getParent() : root) //
                .cmds(args) //
                .masks(getMasks(args.size()));
    }

    private boolean[] getMasks(final int size) {
        boolean[] masks = new boolean[size];
        masks[0] = false;
        System.arraycopy(arguments.toMaskArray(), 0, masks, 1, size - 1);
        return masks;
    }

    private List<String> getArgumentsToList() {
        List<String> argmentsList = this.arguments.toList();
        argmentsList.add(0, command.getRemote());
        return argmentsList;
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
