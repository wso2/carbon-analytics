package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class SinkInfo extends SiddhiElementInfo {

    private String type;
    private String streamId;

    public SinkInfo() {
    }

    public SinkInfo(String id, String name, String definition, String type, String streamId) {
        super(id, name, definition);
        this.type = type;
        this.streamId = streamId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getType() {
        return type;
    }

    public String getStreamId() {
        return streamId;
    }

}
