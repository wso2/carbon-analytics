package org.wso2.carbon.stream.processor.common.exception;

import java.util.Locale;

/**
 * ResourceNotFoundException is used when a resource required is not found
 */
public class ResourceNotFoundException extends Exception {
    /**
     * ResourceType specifies types of resources
     * */
    public enum ResourceType {
        SIDDHI_APP_NAME, STREAM_NAME, CSV_FILE, DATABASE
    }

    private String resourceName;
    private ResourceType resourceType;

    public ResourceNotFoundException(String message, ResourceType resourceType, String resourceName) {
        super(message);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String message,  ResourceType resourceType, String resourceName,
                                     Throwable cause) {
        super(message, cause);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getResourceName() {
        return resourceName;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceTypeString() {
        return resourceType.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
    }
}
