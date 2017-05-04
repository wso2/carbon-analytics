package org.wso2.carbon.stream.processor.common.exception;

import org.wso2.carbon.stream.processor.common.Resources;

import java.util.Locale;

/**
 * ResourceNotFoundException is used when a resource required is not found
 */
public class ResourceNotFoundException extends Exception {

    private String resourceName;
    private Resources.ResourceType resourceType;

    public ResourceNotFoundException(String message, Resources.ResourceType resourceType, String resourceName) {
        super(message);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException(String message,  Resources.ResourceType resourceType, String resourceName,
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

    public Resources.ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceTypeString() {
        return resourceType.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
    }
}
