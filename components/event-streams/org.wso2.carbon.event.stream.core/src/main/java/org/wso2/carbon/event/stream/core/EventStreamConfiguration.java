package org.wso2.carbon.event.stream.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;

public class EventStreamConfiguration {
    private StreamDefinition streamDefinition;
    private boolean isEditable;
    private Object fileName;

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    public void setStreamDefinition(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public Object getFileName() {
        return fileName;
    }

    public void setFileName(Object fileName) {
        this.fileName = fileName;
    }
}
