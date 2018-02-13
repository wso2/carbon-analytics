package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class WindowInfo extends Info {

    // Default Constructor
    public WindowInfo() {
    }

    // Constructor
    public WindowInfo(String id, String name, String definition) {
        setId(id);
        setName(name);
        setDefinition(definition);
    }

}
