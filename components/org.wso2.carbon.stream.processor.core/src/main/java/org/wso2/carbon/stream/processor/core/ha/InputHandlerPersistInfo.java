package org.wso2.carbon.stream.processor.core.ha;

public class InputHandlerPersistInfo {

    private String sourceHandlerId;
    private long timestamp;

    public String getSourceHandlerId() {

        return sourceHandlerId;
    }

    public void setSourceHandlerId(String sourceHandlerId) {

        this.sourceHandlerId = sourceHandlerId;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(long timestamp) {

        this.timestamp = timestamp;
    }
}
