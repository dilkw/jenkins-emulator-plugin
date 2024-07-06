package io.jenkins.plugins.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class CmdRunUtils {

    public static void runBashCommand(String command, String workingDirectory) {
        // try {
        //     // 设置要执行的命令
        //     String[] bashCommand = {"/bin/bash", "-c", command};

        //     // 设置工作目录
        //     ProcessBuilder processBuilder = new ProcessBuilder(bashCommand);
        //     processBuilder.directory(new File(workingDirectory));

        //     // 启动进程
        //     Process process = processBuilder.start();

        //     // 处理标准输出
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //     String line;
        //     while ((line = reader.readLine()) != null) {
        //         System.out.println(line);
        //     }

        //     // 处理错误输出
        //     BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        //     while ((line = errorReader.readLine()) != null) {
        //         System.err.println(line);
        //     }
        //     int exitCode = process.waitFor();
        //     System.out.println("Bash command exited with code" + exitCode);

        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    public void runGradleCommand(String projectPath) {
        // try {
        //     // 设置要执行的命令
        //     String[] command = {"./gradlew", "build"};
            
        //     // 设置工作目录
        //     ProcessBuilder processBuilder = new ProcessBuilder(command);
        //     processBuilder.directory(new File(projectPath));
            
        //     // 启动进程
        //     Process process = processBuilder.start();
            
        //     // 处理标准输出
        //     BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //     String line;
        //     while ((line = reader.readLine()) != null) {
        //         System.out.println(line);
        //     }
    
        //     // 处理错误输出
        //     BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        //     while ((line = errorReader.readLine()) != null) {
        //         System.err.println(line);
        //     }
            
        //     // 等待进程结束并检查退出状态
        //     // int exitCode = process.waitFor();
        //     // System.out.println("Gradle command exited with code " + exitCode);
    
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }
}