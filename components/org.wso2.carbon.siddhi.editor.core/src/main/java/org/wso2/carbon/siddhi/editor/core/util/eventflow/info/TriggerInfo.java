package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class TriggerInfo extends Info {

    // Default Constructor
    public TriggerInfo() {
    }

    // Constructor
    public TriggerInfo(String id, String name, String definition) {
        setId(id);
        setName(name);
        setDefinition(definition);
    }

}
