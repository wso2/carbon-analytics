package org.wso2.carbon.siddhi.editor.core.util.eventflow.info;

import java.util.ArrayList;
import java.util.List;

public class QueryInfo extends Info {

    // Variables
    private List<String> inputStreamIds = new ArrayList<>();
    private String outputStreamId;
    private List<String> functionIds = new ArrayList<>();

    // Default Constructor
    public QueryInfo() {
    }

    // Constructor
    public QueryInfo(String id, String name, String definition, List<String> inputStreamIds, String outputStreamId,
                     List<String> functionsUsed) {
        setId(id);
        setName(name);
        setDefinition(definition);
        setInputStreamIds(inputStreamIds);
        setOutputStreamId(outputStreamId);
        setFunctionIds(functionsUsed);
    }

    // Setters
    public void setInputStreamIds(List<String> inputStreamIds) {
        this.inputStreamIds = inputStreamIds;
    }

    public void setOutputStreamId(String outputStreamId) {
        this.outputStreamId = outputStreamId;
    }

    public void setFunctionIds(List<String> functionIds) {
        this.functionIds = functionIds;
    }

    // Getters
    public List<String> getInputStreamIds() {
        return inputStreamIds;
    }

    public String getOutputStreamId() {
        return outputStreamId;
    }

    public List<String> getFunctionIds() {
        return functionIds;
    }

    // functionIds List Handlers
    public void addFunctionId(String functionId) {
        functionIds.add(functionId);
    }

}
