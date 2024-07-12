/*
 * The MIT License
 *
 * Copyright (c) 2020, Nikolas Falco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.sample.cmd.help;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Node;
import io.jenkins.plugins.sample.Messages;
import io.jenkins.plugins.sample.sdk.DetectionFailedException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * Supported platform.
 */
public enum Platform {
    LINUX(".sh", "bin"), WINDOWS(".bat", "bin"), OSX(".sh", "bin");

    /**
     * Choose the extension file name suitable to run cli commands.
     */
    public final String extension;
    /**
     * Choose the folder path suitable bin folder of the bundle.
     */
    public final String binFolder;

    Platform(String extension, String binFolder) {
        this.extension = extension;
        this.binFolder = binFolder;
    }

    public boolean is(String line) {
        return line.contains(name());
    }

    public static Platform of(Node node) throws InterruptedException, IOException, DetectionFailedException {
        try {
            Computer computer = node.toComputer();
            if (computer == null) {
                throw new Exception();
            }
            return detect(computer.getSystemProperties());
        } catch (Exception e) {
            throw new IOException();
        }
    }

    public static Platform fromWorkspace(FilePath workspace)  throws InterruptedException, IOException {
        Computer computer = workspace.toComputer();
        if (computer == null) {
            throw new AbortException(Messages.NODE_NOT_AVAILABLE());
        }
        Node node = computer.getNode();
        if (node == null) {
            throw new AbortException(Messages.NODE_NOT_AVAILABLE());
        }
        return of(node);
    }

    public static Platform fromCurrentComputer() throws Exception {
        Computer computer = Computer.currentComputer();
        if (computer == null) {
            throw new AbortException(Messages.NODE_NOT_AVAILABLE());
        }
        Node node = computer.getNode();
        if (node == null) {
            throw new AbortException(Messages.NODE_NOT_AVAILABLE());
        }
        return of(node);
    }

    public static Platform fromFileRoot(String fileRoot) throws Exception {
        File file = new File(fileRoot);
        if (!file.exists()) throw new Exception();
        FilePath filePath = new FilePath(file);
        return fromWorkspace(filePath);
    }

    public static Platform currentPlatform() throws Exception {
        return detect(System.getProperties());
    }

    private static Platform detect(Map<Object, Object> systemProperties) throws Exception {
        String arch = ((String) systemProperties.get("os.name")).toLowerCase(Locale.ENGLISH);
        if (arch.contains("linux")) {
            return LINUX;
        }
        if (arch.contains("windows")) {
            return WINDOWS;
        }
        if (arch.contains("mac")) {
            return OSX;
        }
        throw new Exception(Messages.OS_UNSUPPORTED());
    }

}
