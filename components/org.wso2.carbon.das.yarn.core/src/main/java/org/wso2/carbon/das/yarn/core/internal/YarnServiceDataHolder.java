package org.wso2.carbon.das.yarn.core.internal;
import org.wso2.carbon.das.yarn.core.bean.YarnConfig;

/**
 * YarnServiceDataHolder holds {@link YarnConfig} which contains configuration details for Yarn deployment.
 */
public class YarnServiceDataHolder {
    private static YarnConfig yarnConfig;

    public static YarnConfig getYarnConfig() {
        return yarnConfig;
    }

    public static void setYarnConfig(YarnConfig yarnConfig) {
        YarnServiceDataHolder.yarnConfig = yarnConfig;
    }
}
