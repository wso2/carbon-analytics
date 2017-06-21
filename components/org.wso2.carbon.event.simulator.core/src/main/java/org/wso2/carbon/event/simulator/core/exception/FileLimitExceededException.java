package org.wso2.carbon.event.simulator.core.exception;


/**
 * customized exception class for when size of file uploaded exceeds the size limit
 */
public class FileLimitExceededException extends RuntimeException {

    /**
     * Throws customizes extensions for when size of file uploaded exceeds the size limit
     *
     * @param message Error Message
     */
    public FileLimitExceededException(String message) {
        super(message);

    } public FileLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
