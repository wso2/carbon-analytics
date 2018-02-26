package org.wso2.carbon.siddhi.editor.core.util.eventflow.constants;

public enum EdgeType {
    DEFAULT("arrow"),
    DOTTED_LINE("dotted-line");

    private String type;

    EdgeType(String type) {
        this.type = type;
    }

    public String getTypeAsString() {
        return type;
    }

}
