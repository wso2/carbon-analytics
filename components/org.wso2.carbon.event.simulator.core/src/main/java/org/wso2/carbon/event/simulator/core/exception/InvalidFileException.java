package org.wso2.carbon.event.simulator.core.exception;

/**
 * customized exception class for uploading non csv files
 */
public class InvalidFileException extends Exception {

    /**
     * Throws customizes exception for uploading invalid files
     *
     * @param message Error Message
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Throws customizes exception for uploading invalid files
     *
     * @param message Error Message
     * @param cause   Throwable that caused the InvalidConfigException
     */
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
