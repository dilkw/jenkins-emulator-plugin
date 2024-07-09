package io.jenkins.plugins.sample;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class HelloWorldAction implements RunAction2 {

    private String name;

    @SuppressWarnings("rawtypes")
    private transient Run run;

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    @SuppressWarnings("rawtypes")
    public Run getRun() {
        return run;
    }

    public HelloWorldAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/dilkw/images/icon_test_report.png";
    }

    @Override
    public String getDisplayName() {
        return "Greeting";
    }

    @Override
    public String getUrlName() {
        return "greeting";
    }
}
