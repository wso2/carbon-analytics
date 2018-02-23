package org.wso2.carbon.siddhi.editor.core.util.eventflow.constants;

public enum NodeType {
    AGGREGATION("aggregation"),
    FUNCTION("function"),
    PARTITION("partition"),
    PARTITION_TYPE("partition-type"),
    QUERY("query"),
    STREAM("stream"),
    TABLE("table"),
    TRIGGER("trigger"),
    WINDOW("window");

    private String constant;

    NodeType(String constant) {
        this.constant = constant;
    }
}
