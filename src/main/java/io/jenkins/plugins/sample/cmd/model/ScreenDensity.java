package io.jenkins.plugins.sample.cmd.model;

import hudson.Util;

public enum ScreenDensity {

    LOW(120, "ldpi"),
    MEDIUM(160, "mdpi"),
    TV_720P(213, "tvdpi"),
    HIGH(240, "hdpi"),
    EXTRA_HIGH(320, "xhdpi"),
    EXTRA_HIGH_400(400, ""),
    EXTRA_HIGH_420(420, ""),
    EXTRA_EXTRA_HIGH(480, "xxhdpi"),
    EXTRA_EXTRA_HIGH_560(560, ""),
    EXTRA_EXTRA_EXTRA_HIGH(640, "xxxhdpi");

    private static final ScreenDensity[] PRESETS = { LOW, MEDIUM, TV_720P, HIGH,
            EXTRA_HIGH, EXTRA_HIGH_400, EXTRA_HIGH_420, EXTRA_EXTRA_HIGH, EXTRA_EXTRA_HIGH_560,
            EXTRA_EXTRA_EXTRA_HIGH };

    private final int dpi;
    private final String alias;
    ScreenDensity(int dpi, String alias) {
        this.dpi = dpi;
        this.alias = alias;
    }

    public int getDpi() {
        return dpi;
    }

    public String getAlias() {
        return alias;
    }

    public static ScreenDensity fromValue(int dpi) {
        for (ScreenDensity screenDensity : ScreenDensity.values()) {
            if (screenDensity.getDpi() == dpi) {
                return screenDensity;
            }
        }
        return null;
    }

    public static ScreenDensity valueOfDensity(String density) {
        if (Util.fixEmptyAndTrim(density) == null) {
            return null;
        } else {
            density = density.toLowerCase();
        }

        for (ScreenDensity preset : PRESETS) {
            if (density.equals(preset.alias) || density.equals(preset.toString())) {
                return preset;
            }
        }

        // Return custom value, if things look valid
        try {
            int dpi = Integer.parseInt(density);
            return fromValue(dpi);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
