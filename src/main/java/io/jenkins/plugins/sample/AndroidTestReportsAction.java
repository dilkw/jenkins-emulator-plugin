package io.jenkins.plugins.sample;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class AndroidTestReportsAction implements RunAction2 {

    private String reportPath;
    private String name;
    private String testResultsPath;

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

    @SuppressWarnings("rawtypes")
    private transient Run run;


    public AndroidTestReportsAction(Run<?, ?> run, String archiveSourceRoot) {
        this.run = run;
        this.reportPath = archiveSourceRoot;
        this.testResultsPath = archiveSourceRoot;
        //"app/build/reports/androidTests/connected/debug/index.html";
    }
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

    @Override
    public String getDisplayName() {
        return "Test Report";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/dilkw/images/icon_test_report.png";
    }

    @Override
    public String getUrlName() {
        return "greeting";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTestResultsPath() {
        return testResultsPath;
    }

    public void setTestResultsPath(String testResultsPath) {
        this.testResultsPath = testResultsPath;
    }

    public void setRun(Run run) {
        this.run = run;
    }
}
