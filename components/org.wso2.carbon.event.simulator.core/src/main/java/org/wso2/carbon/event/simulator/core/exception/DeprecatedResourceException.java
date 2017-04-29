package org.wso2.carbon.event.simulator.core.exception;

import org.wso2.carbon.stream.processor.common.Resources;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;

import java.util.Locale;

/**
 * Customized exception class for when resources required for a simulation are not found when starting a simulator
 * i.e. the resource was available when the simulator was created but was deleted afterwards, hence resource is not
 * available at the point of starting the simulator
 */
public class DeprecatedResourceException extends RuntimeException {

    Resources.ResourceType resourceType;
    String resourceName;
    /**
     * Throws customizes deprecated resource exception
     *
     * @param message Error Message
     */
    public DeprecatedResourceException(String message) {
        super(message);
    }

    /**
     * Throws customizes deprecated resource exception
     *
     * @param message Error Message
     * @param cause   throwable which caused the event generation exception
     */
    public DeprecatedResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeprecatedResourceException(String message, Resources.ResourceType resourceType, String
            resourceName, Throwable cause) {
        super(message, cause);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public Resources.ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceTypeString() {
        return resourceType.toString().toLowerCase(Locale.ENGLISH).replace("_", " ");
    }

    public String getResourceName() {
        return resourceName;
    }
}
