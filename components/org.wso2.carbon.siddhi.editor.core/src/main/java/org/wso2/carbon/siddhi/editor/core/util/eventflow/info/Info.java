package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public abstract class Info {

    // TODO: 2/1/18 Add License Headers for all the classes
    // TODO: 2/1/18 Organize imports for all the files 
    
    // Variables
    private String id;
    private String name;
    private String definition;

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

}
