package io.jenkins.plugins.sample.cmd.help;

public enum Channel {
    STABLE(0, "Stable"), BETA(1, "Beta"), DEV(2, "Dev"), CANARY(3, "Canary");

    private final int value;
    private final String label;

    Channel(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
