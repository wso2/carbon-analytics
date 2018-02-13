package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class FunctionInfo extends Info {
    
    // Default Constructor
    public FunctionInfo() {
    }

    // Constructor
    public FunctionInfo(String id, String name, String definition) {
        setId(id);
        setName(name);
        setDefinition(definition);
    }
    
}
