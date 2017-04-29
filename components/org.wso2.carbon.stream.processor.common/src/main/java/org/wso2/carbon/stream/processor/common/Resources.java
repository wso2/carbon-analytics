package org.wso2.carbon.stream.processor.common;

/**
 * Resources interface specifies the resources used for simulation
 */
public interface Resources {
    /**
     * ResourceType specifies types of resources
     */
    public enum ResourceType {
        EXECUTION_PLAN, STREAM, CSV_FILE
    }
}
