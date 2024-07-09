package io.jenkins.plugins.sample.cmd;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.cli.CLICommand;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.util.ArgumentListBuilder;
import hudson.util.StreamTaskListener;
import org.apache.commons.io.input.NullInputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SDKCLICommand<R> extends CLICommand {

    private ArgumentListBuilder arguments;

    private FilePath command;

    private final Map<String, String> env;
    private InputStream stdin = new NullInputStream(0);
    private FilePath root;
    //private OutputParser<R> parser;

    public ArgumentListBuilder getArguments() {
        return arguments;
    }

    public void setArguments(ArgumentListBuilder arguments) {
        this.arguments = arguments;
    }

    SDKCLICommand(FilePath command, ArgumentListBuilder arguments, Map<String, String> env) {
        this.command = command;
        this.arguments = arguments;
        this.env = env;
    }

    @Override
    public String getShortDescription() {
        return "";
    }

    @Override
    protected int run() throws Exception {

        User user = User.current();
        // 执行一些自定义逻辑
        if (user != null) {
            stdout.println("Hello, " + user.getId() + "!");
        } else {
            stdout.println("Hello, anonymous user!");
        }

        // 返回退出代码
        return 0;
    }

    public void installSDK() throws Exception {



    }

    public R execute() throws IOException, InterruptedException {
        return execute(new StreamTaskListener(OutputStream.nullOutputStream(), StandardCharsets.UTF_8));
    }

    public R execute(@NonNull TaskListener output) throws IOException, InterruptedException {
        ArgumentListBuilder args = getArguments();

        // command.createLauncher(output)
        Launcher.ProcStarter starter = command.createLauncher(output).launch() //
                .envs(env) //
                .stdin(stdin) //
                .pwd(root == null ? command.getParent() : root) //
                .cmds(args) //
                .masks(args.toMaskArray());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        if (output != null) {
//            if (parser != null) {
//                // clone output to make content available to the parser
//                starter.stdout(new ForkOutputStream(output.getLogger(), baos));
//            } else {
//                starter.stdout(output);
//            }
//        } else if (parser != null) {
//            starter.stdout(baos);
//        }

        starter.stdout(output);

        int exitCode = starter.join();
        if (exitCode != 0) {
            throw new IOException(command.getBaseName() + " " + arguments.toString() + " failed. exit code: " + exitCode + ".");
        }

//        if (parser != null) {
//            return parser.parse(new ByteArrayInputStream(baos.toByteArray()));
//        }
        return null;
    }
}
