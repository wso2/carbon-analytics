package org.wso2.carbon.siddhi.editor.core.util.eventflow.constants;

public enum SiddhiAnnotationType {
    DESCRIPTION("description"),
    INFO("info"),
    NAME("name");

    private String constant;

    SiddhiAnnotationType(String constant) {
        this.constant = constant;
    }
}
