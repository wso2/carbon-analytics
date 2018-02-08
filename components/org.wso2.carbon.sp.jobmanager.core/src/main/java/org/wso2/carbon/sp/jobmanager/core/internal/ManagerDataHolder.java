package org.wso2.carbon.sp.jobmanager.core.internal;

import com.zaxxer.hikari.HikariDataSource;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.sp.jobmanager.core.dbhandler.ManagerDeploymentConfig;

public class ManagerDataHolder {
    private static ManagerDataHolder instance = new ManagerDataHolder();
    private ConfigProvider configProvider;
    private HikariDataSource dashboardManagerDataSource;
    private PermissionProvider permissionProvider;
    private ManagerDeploymentConfig managerDeploymentConfig = new ManagerDeploymentConfig();


    private ManagerDataHolder() {

    }

    /**
     * Provide instance of ManagerDataholder class.
     *
     * @return Instance of ManagerDataHolder
     */
    public static ManagerDataHolder getInstance() {
        return instance;
    }

    /**
     * Return Config provider
     *
     * @return Instance of the config reader
     */

    public ConfigProvider getConfigProvider() {
        return this.configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public HikariDataSource getDashboardManagerDataSource() {
        return dashboardManagerDataSource;
    }

    public void setDashboardManagerDataSource(HikariDataSource dashboardManagerDataSource) {
        this.dashboardManagerDataSource = dashboardManagerDataSource;
    }

    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    public void setPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    public ManagerDeploymentConfig getManagerDeploymentConfig() {
        return managerDeploymentConfig;
    }

    public void setManagerDeploymentConfig(
            ManagerDeploymentConfig managerDeploymentConfig) {
        this.managerDeploymentConfig = managerDeploymentConfig;
    }
}
