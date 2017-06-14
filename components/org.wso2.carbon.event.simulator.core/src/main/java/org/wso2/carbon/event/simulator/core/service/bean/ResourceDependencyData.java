package org.wso2.carbon.event.simulator.core.service.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;

/**
 * ResourceDependencyData holds details about the resource required by the inactive simulation
 */
public class ResourceDependencyData {

    private ResourceNotFoundException.ResourceType resourceType;
    private String resourceName;

    public ResourceDependencyData(ResourceNotFoundException.ResourceType resourceType, String resourceName) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException.ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceNotFoundException.ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ResourceDependencyData)) {
            return false;
        } else {
            ResourceDependencyData resourceData = (ResourceDependencyData) obj;
            return new EqualsBuilder()
                    .append(resourceType, resourceData.resourceType)
                    .append(resourceName, resourceData.resourceName)
                    .isEquals();
        }
    }
}
