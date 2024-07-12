
package io.jenkins.plugins.sample.cmd.model;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import io.jenkins.plugins.sample.Messages;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class HardwareProperty extends AbstractDescribableImpl<HardwareProperty> {

    private final String key;
    private final String value;

    @DataBoundConstructor
    public HardwareProperty(String key, String value) {
        this.key = Util.fixEmptyAndTrim(key);
        this.value = Util.fixEmptyAndTrim(value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Symbol("hwProperty")
    @Extension
    public static final class DescriptorImpl extends Descriptor<HardwareProperty> {
        @Override
        public String getDisplayName() {
            return "Property";
        }

        public FormValidation doCheckKey(@QueryParameter String key) {
            if (StringUtils.isBlank(key)) {
                return FormValidation.error(Messages.required());
            }
            return FormValidation.ok();
        }
    }

}