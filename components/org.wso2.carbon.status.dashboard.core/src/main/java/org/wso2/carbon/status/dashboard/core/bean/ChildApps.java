package org.wso2.carbon.status.dashboard.core.bean;

public class ChildApps {
    private String parentAppName;
    private String appName;
    private String siddhiApp;

    private ChildApps() {

    }

    public String getParentAppName() {
        return parentAppName;
    }

    public void setParentAppName(String parentAppName) {
        this.parentAppName = parentAppName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSiddhiApp() {
        return siddhiApp;
    }

    public void setSiddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
    }
}
