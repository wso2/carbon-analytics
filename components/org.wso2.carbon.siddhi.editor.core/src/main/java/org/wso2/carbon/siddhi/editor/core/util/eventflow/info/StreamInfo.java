package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class StreamInfo extends Info {

    // Default Constructor
    public StreamInfo() {
    }

    // Constructor
    public StreamInfo(String id, String name, String definition) {
        setId(id);
        setName(name);
        setDefinition(definition);
    }

}
