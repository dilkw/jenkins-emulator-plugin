package io.jenkins.plugins.sample.cmd.model;

public class ADBDevice {
    String emulatorName;
    String status;// offline / device


    public ADBDevice(String emulatorName, String status) {
        this.emulatorName = emulatorName;
        this.status = status;
    }

    public String getEmulatorName() {
        return emulatorName;
    }

    public void setEmulatorName(String emulatorName) {
        this.emulatorName = emulatorName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
