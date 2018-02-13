package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class PartitionTypeInfo extends Info {

    // Variables
    private String streamId;

    // Default Constructor
    public PartitionTypeInfo() {
    }

    // Constructor
    public PartitionTypeInfo(String id, String name, String definition, String streamId) {
        setId(id);
        setName(name);
        setDefinition(definition);
        setStreamId(streamId);
    }

    // Setters
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    // Getters
    public String getStreamId() {
        return streamId;
    }

}
