package org.wso2.carbon.stream.processor.common.exception;

/**
 * ResourceNotFoundException is used when a resource required is not found
 */
public class ResourceNotFoundException extends Exception {
    /**
     * ResourceType specifies types of resources
     * */
    public enum ResourceType {
        SIDDHI_APP_NAME, STREAM_NAME}

    private String resourceName;
    private ResourceType resourceType;

    public ResourceNotFoundException(String message, ResourceType resourceType, String resourceName) {
        super(message);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
}
