package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import hudson.util.ForkOutputStream;
import hudson.util.StreamTaskListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.input.NullInputStream;

public class ChristelleCLICommand<R> extends hudson.cli.CLICommand {
    private final ArgumentListBuilder arguments;
    private final FilePath command;
    private final Map<String, String> env;
    private final InputStream stdin = new NullInputStream(0);
    private final FilePath root;
    private OutputParser<R> parser;

    public ArgumentListBuilder getArguments() {
        return arguments;
    }

    ChristelleCLICommand(FilePath command, ArgumentListBuilder arguments, Map<String, String> env, FilePath root) {
        this.command = command;
        this.arguments = arguments;
        this.env = env;
        this.root = root;
    }

    @Override
    public String getShortDescription() {
        return "";
    }

    @Override
    public int run() throws Exception {
        execute();
        // 返回退出代码
        return 0;
    }

    public R execute() throws IOException, InterruptedException {
        return execute(new StreamTaskListener(OutputStream.nullOutputStream(), StandardCharsets.UTF_8));
    }

    public R execute(@NonNull TaskListener output) throws IOException, InterruptedException {
        ArgumentListBuilder args = getArguments();

        // command.createLauncher(output)

        Launcher launcher = command.createLauncher(output);
        launcher.isUnix();
        Launcher.ProcStarter starter = launcher
                .launch() //
                .envs(env) //
                .stdin(stdin) //
                .pwd(root == null ? command.getParent() : root) //
                .cmds(args) //
                .masks(args.toMaskArray());
        starter.stdout(output);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (parser != null) {
            // clone output to make content available to the parser
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

    public ChristelleCLICommand<R> withParser(OutputParser<R> parser) {
        this.parser = parser;
        return this;
    }


    public interface OutputParser<R> {
        R parse(InputStream input) throws IOException;
    }

}
