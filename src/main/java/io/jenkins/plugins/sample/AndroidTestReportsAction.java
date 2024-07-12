package io.jenkins.plugins.sample;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class AndroidTestReportsAction implements RunAction2 {

    private String reportPath;

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }

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

    public AndroidTestReportsAction(String reportPath) {
        this.reportPath = reportPath;
    }

    @Override
    public String getDisplayName() {
        return "Test Report";
    }

    @Override
    public String getIconFileName() {
        return "/plugin/christelle/images/icon_test_report.png";
    }

    @Override
    public String getUrlName() {
        return "greeting";
    }
}
