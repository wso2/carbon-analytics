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

    private String type;

    NodeType(String type) {
        this.type = type;
    }

    public String getTypeAsString() {
        return type;
    }

}
