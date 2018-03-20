package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class SourceInfo extends SiddhiElementInfo {

    private String type;
    private String streamId;

    public SourceInfo() {
    }

    public SourceInfo(String id, String name, String definition, String type, String streamId) {
        super(id, name, definition);
        this.type = type;
        this.streamId = streamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

}
