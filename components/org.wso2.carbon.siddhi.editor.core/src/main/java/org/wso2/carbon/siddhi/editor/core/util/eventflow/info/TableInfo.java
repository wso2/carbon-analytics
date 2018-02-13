package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class TableInfo extends Info{

    // Default Constructor
    public TableInfo() {
    }

    // Constructor
    public TableInfo(String id, String name, String definition) {
        setId(id);
        setName(name);
        setDefinition(definition);
    }

}
