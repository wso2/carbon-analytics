package org.wso2.carbon.sp.jobmanager.core.dbhandler;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.database.query.manager.config.Queries;
import org.wso2.carbon.sp.jobmanager.core.bean.ManagerAccessCredentials;
import org.wso2.carbon.sp.jobmanager.core.bean.ManagerConnectionConfigurations;

import java.util.ArrayList;

@Configuration(namespace = "wso2.sp.status", description = "WSO2 status dashboard manager query "
        + "provider")
public class ManagerDeploymentConfig {
    @Element(description = "Admin Username across cluster")
    private ManagerAccessCredentials managerAccessCredentials = new ManagerAccessCredentials();

    @Element(description = "Feign client configurations.")
    private ManagerConnectionConfigurations managerConnectionConfigurations = new ManagerConnectionConfigurations();

    @Element(description = "polling interval to get real-time statistics of worker in seconds")
    private Integer pollingInterval;
    @Element(description = "Dasboard ManagerDataSource.")
    private String dashboardManagerDatasourceName;

    @Element(description = "Database query map")
    private ArrayList<Queries> queries;

    public ManagerDeploymentConfig() {

    }

    public ArrayList<Queries> getQueries() {
        return queries;
    }

    public void setQueries(ArrayList<Queries> queries) {
        this.queries = queries;
    }

    public String getDashboardManagerDatasourceName() {
        return dashboardManagerDatasourceName;
    }

    public void setDashboardManagerDatasourceName(String dashboardManagerDatasourceName) {
        this.dashboardManagerDatasourceName = dashboardManagerDatasourceName;
    }

    public ManagerAccessCredentials getManagerAccessCredentials() {
        return managerAccessCredentials;
    }

    public void setManagerAccessCredentials(
            ManagerAccessCredentials managerAccessCredentials) {
        this.managerAccessCredentials = managerAccessCredentials;
    }

    public ManagerConnectionConfigurations getManagerConnectionConfigurations() {
        return managerConnectionConfigurations;
    }

    public void setManagerConnectionConfigurations(
            int connectionTimeOut, int readTimeOut) {
        this.managerConnectionConfigurations = new ManagerConnectionConfigurations(connectionTimeOut, readTimeOut);
    }

    public Integer getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(Integer pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getUsername() {
        return managerAccessCredentials.getUsername();
    }

    public void setUsername(String username) {
        managerAccessCredentials.setUsername(username);
    }

    public String getPassword() {
        return managerAccessCredentials.getPassword();
    }

    public void setPassword(String password) {
        managerAccessCredentials.setPassword(password);
    }
}
