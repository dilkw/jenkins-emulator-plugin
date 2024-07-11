package io.jenkins.plugins.sample.cmd;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.util.ArgumentListBuilder;
import io.jenkins.plugins.sample.Constants;
import io.jenkins.plugins.sample.cmd.help.Utils;
import org.apache.commons.io.input.NullInputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ADBManagerCLIBuilder {

    private String adbRoot = "";

    private FilePath command;
    private EnvVars env;
    private final InputStream stdin = new NullInputStream(0);
    private FilePath root;

    private final List<ChristelleCLICommand<Objects>> christelleCLICommandList = new ArrayList<>();

//    public ADBManagerCLIBuilder startServer() {
//        FilePath command = Utils.getExecutable()
//        christelleCLICommandList.add();
//        return this;
//    }

//    public ADBManagerCLIBuilder waitForDevice() {
//
//    }
//
//    public ADBManagerCLIBuilder buildCommand() {
//        return new ChristelleCLICommand<>(command, args, )
//    }
//
//    public ADBManagerCLIBuilder buildCommand(String arg) {
//        return new ChristelleCLICommand<>(command, args, )
//    }
//
//    public ArgumentListBuilder getArguments() {
//        ArgumentListBuilder builder = new ArgumentListBuilder();
//        return builder;
//    }

}
