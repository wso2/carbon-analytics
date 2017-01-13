package org.wso2.analytics.indexerservice.exceptions;

/**
 * This exception is thrown when a given index schema is not found
 */
public class IndexSchemaNotFoundException extends Exception {

    public IndexSchemaNotFoundException(String message) {
        super(message);
    }

    public IndexSchemaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
