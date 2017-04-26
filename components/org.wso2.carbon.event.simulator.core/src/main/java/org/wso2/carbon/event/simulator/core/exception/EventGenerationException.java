package org.wso2.carbon.event.simulator.core.exception;

/**
 * customized exception class for event generation
 */
public class EventGenerationException extends RuntimeException {

    /**
     * Throws customizes event generation exception
     *
     * @param message Error Message
     */
    public EventGenerationException(String message) {
        super(message);
    }

    /**
     * Throws customizes event generation exception
     *
     * @param message Error Message
     * @param cause   throwable which caused the event generation exception
     */
    public EventGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

}
