package org.wso2.carbon.sp.jobmanager.core.bean;

import java.util.List;

/**
 *
 */
public class KafkaTransportDetails {
    private String appName;
    private String siddhiApp;
    private List<String> sourceList;
    private List<String> sinkList;

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

    public List<String> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<String> sourceList) {
        this.sourceList = sourceList;
    }

    public List<String> getSinkList() {
        return sinkList;
    }

    public void setSinkList(List<String> sinkList) {
        this.sinkList = sinkList;
    }
}
