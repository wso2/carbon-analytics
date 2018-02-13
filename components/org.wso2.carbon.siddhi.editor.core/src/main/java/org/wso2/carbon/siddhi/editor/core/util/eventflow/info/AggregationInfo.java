package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

public class AggregationInfo extends Info {

    // Variables
    private String inputStreamId;

    // Default Constructor
    public AggregationInfo() {
    }

    // Constructor
    public AggregationInfo(String id, String name, String definition, String inputStreamId) {
        setId(id);
        setName(name);
        setDefinition(definition);
        setInputStreamId(inputStreamId);
    }

    // Setters
    public void setInputStreamId(String inputStreamId) {
        this.inputStreamId = inputStreamId;
    }

    // Getters
    public String getInputStreamId() {
        return inputStreamId;
    }

}
