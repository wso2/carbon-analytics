package org.wso2.carbon.data.provider.siddhi.config;

import org.wso2.carbon.data.provider.ProviderConfig;

public class SiddhiDataProviderConfig implements ProviderConfig {

    private String siddhiApp;
    private String query;
    private int publishingInterval;


    public SiddhiDataProviderConfig() {
        this.query = "";
        this.siddhiApp = "";
        this.publishingInterval = 5;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSiddhiAppContext() {
        return siddhiApp;
    }

    public void setSiddhiAppContext(String siddhiAppContext) {
        this.siddhiApp = siddhiAppContext;
    }

    @Override
    public long getPublishingInterval() {
        return this.publishingInterval;
    }

    @Override
    public long getPurgingInterval() {
        return 0;
    }

    @Override
    public boolean isPurgingEnable() {
        return false;
    }
}
