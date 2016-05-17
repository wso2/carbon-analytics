package org.wso2.carbon.event.execution.manager.core.structure.configuration;

import java.util.List;

public class StreamMapping {
    private String fromStream;
    private String toStream;
    private List<String[]> attributePairs;

    public String getFromStream() {
        return fromStream;
    }

    public void setFromStream(String fromStream) {
        this.fromStream = fromStream;
    }

    public String getToStream() {
        return toStream;
    }

    public void setToStream(String toStream) {
        this.toStream = toStream;
    }

    public List<String[]> getAttributePairs() {
        return attributePairs;
    }

    public void setAttributePairs(List<String[]> attributePairs) {
        this.attributePairs = attributePairs;
    }
}
