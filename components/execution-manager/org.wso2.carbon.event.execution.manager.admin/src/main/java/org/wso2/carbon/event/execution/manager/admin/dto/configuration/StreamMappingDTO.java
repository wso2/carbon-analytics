package org.wso2.carbon.event.execution.manager.admin.dto.configuration;

/**
 * DTO class of StreamMapping for ExecutionManagerAdminService
 */
public class StreamMappingDTO {

    private String fromStream;
    private String toStream;
    private String[][] attributePairs;

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

    public String[][] getAttributePairs() {
        return attributePairs;
    }

    public void setAttributePairs(String[][] attributePairs) {
        this.attributePairs = attributePairs;
    }
}
