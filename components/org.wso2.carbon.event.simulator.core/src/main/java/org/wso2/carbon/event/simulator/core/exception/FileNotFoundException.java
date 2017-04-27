package org.wso2.carbon.event.simulator.core.exception;

/**
 * customized exception class for when file requested is not found
 */
public class FileNotFoundException extends Exception {

    /**
     * Throws customizes exception for when file requested is not found
     *
     * @param message Error Message
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
