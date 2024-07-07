package io.jenkins.plugins.sample;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

@Extension
public class AndroidSDKBuilder extends GlobalConfiguration {
    private String mySetting;
    private boolean myFlag;

    @DataBoundConstructor
    public AndroidSDKBuilder() {
        load(); // Load persisted configuration
    }

    public static AndroidSDKBuilder get() {
        return GlobalConfiguration.all().get(AndroidSDKBuilder.class);
    }

    public String getMySetting() {
        return mySetting;
    }

    public void setMySetting(String mySetting) {
        this.mySetting = mySetting;
        save(); // Save the updated configuration
    }

    public boolean isMyFlag() {
        return myFlag;
    }

    public void setMyFlag(boolean myFlag) {
        this.myFlag = myFlag;
        save(); // Save the updated configuration
    }

    @Override
    public String getDisplayName() {
        return "My Global Configuration";
    }
}
