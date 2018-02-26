package org.wso2.carbon.siddhi.editor.core.util.eventflow.constants;

public enum SiddhiAnnotationType {
    DESCRIPTION("description"),
    INFO("info"),
    NAME("name");

    private String type;

    SiddhiAnnotationType(String type) {
        this.type = type;
    }

    public String getTypeAsString() {
        return type;
    }

}
