package io.jenkins.plugins.sample.cmd.model;

import hudson.Util;
import io.jenkins.plugins.sample.Constants;

public enum ScreenResolution {

    QVGA(240, 320, "QVGA", "QVGA"),
    WQVGA(240, 400, "WQVGA", "WQVGA400"),
    FWQVGA(240, 432, "FWQVGA", "WQVGA432"),
    HVGA(320, 480, "HVGA", "HVGA"),
    WVGA(480, 800, "WVGA", "WVGA800"),
    FWVGA(480, 854, "FWVGA", "WVGA854"),
    WSVGA(1024, 654, "WSVGA", "WSVGA"),
    WXGA_720(1280, 720, "WXGA720", "WXGA720"),
    WXGA_800(1280, 800, "WXGA800", "WXGA800"),
    WXGA(1280, 800, "WXGA", "WXGA");


    private static final ScreenResolution[] PRESETS = { QVGA, WQVGA, FWQVGA, HVGA,
                                                                       WVGA, FWVGA, WSVGA,
                                                                       WXGA_720, WXGA_800, WXGA };

    private final int width;
    private final int height;
    private final String alias;
    private final String skinName;

    ScreenResolution(int width, int height, String alias, String skinName) {
        this.width = width;
        this.height = height;
        this.alias = alias;
        this.skinName = skinName;
    }

    public static ScreenResolution valueOf(int width, int height) {
        for (ScreenResolution resolution : PRESETS) {
            if (resolution.height == height && resolution.width == width) {
                return resolution;
            }
        }
        return null;
    }

    public static ScreenResolution valueOfResolution(String resolution) {
        if (Util.fixEmptyAndTrim(resolution) == null) {
            return null;
        }

        // Try matching against aliases
        for (ScreenResolution preset : PRESETS) {
            if (resolution.equalsIgnoreCase(preset.alias)) {
                return preset;
            }
        }

        // Check for pixel values
        resolution = resolution.toLowerCase();
        if (!resolution.matches(Constants.REGEX_SCREEN_RESOLUTION)) {
            return null;
        }

        // Try matching against pixel values
        int index = resolution.indexOf('x');
        int width = 0;
        int height = 0;
        try {
            width = Integer.parseInt(resolution.substring(0, index));
            height = Integer.parseInt(resolution.substring(index+1));
        } catch (NumberFormatException ex) {
            return null;
        }
        for (ScreenResolution preset : PRESETS) {
            if (width == preset.width && height == preset.height) {
                return preset;
            }
        }

        // Return custom value
        return valueOf(width, height);
    }


    private ScreenResolution(int width, int height) {
        this(width, height, null, null);
    }

    public boolean isCustomResolution() {
        return alias == null;
    }

    public String getSkinName() {
        if (isCustomResolution()) {
            return getDimensionString();
        }

        return skinName;
    }

    public String getDimensionString() {
        return width +"x"+ height;
    }

    @Override
    public String toString() {
        if (isCustomResolution()) {
            return getDimensionString();
        }

        return alias;
    }

}