package io.jenkins.plugins.sample.cmd.model;

import hudson.model.Action;

public class AndroidEmulatorShareDataAction implements Action {

    int emulatorConsolePort;

    public AndroidEmulatorShareDataAction(int emulatorConsolePort) {
        this.emulatorConsolePort = emulatorConsolePort;
    }

    public int getEmulatorConsolePort() {
        return emulatorConsolePort;
    }

    @Override
    public String getIconFileName() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getUrlName() {
        return "";
    }
}
