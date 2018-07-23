package org.wso2.carbon.stream.processor.core.ha;

public class SiddhiAppPersistInfo {

    private String siddhiAppId;
    private long timestamp;

    public String getSiddhiAppId() {

        return siddhiAppId;
    }

    public void setSiddhiAppId(String siddhiAppId) {

        this.siddhiAppId = siddhiAppId;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }
}
