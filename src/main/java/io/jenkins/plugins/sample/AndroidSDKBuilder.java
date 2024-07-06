package io.jenkins.plugins.sample;

import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;

@Extension
public class AndroidSDKBuilder extends GlobalConfiguration  {

    private String someProperty;
    private boolean enableFeatureX;
    private int timeout;

    @DataBoundConstructor
    public AndroidSDKBuilder() {
        // Loads the configuration from the disk
        load();
    }

    public static AndroidSDKBuilder get() {
        return GlobalConfiguration.all().get(AndroidSDKBuilder.class);
    }

    public String getSomeProperty() {
        return someProperty;
    }

    public void setSomeProperty(String someProperty) {
        this.someProperty = someProperty;
        save();
    }

    public boolean isEnableFeatureX() {
        return enableFeatureX;
    }

    public void setEnableFeatureX(boolean enableFeatureX) {
        this.enableFeatureX = enableFeatureX;
        save();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        save();
    }
}
