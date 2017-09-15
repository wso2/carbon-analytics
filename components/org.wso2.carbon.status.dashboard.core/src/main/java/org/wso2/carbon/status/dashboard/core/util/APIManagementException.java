package org.wso2.carbon.status.dashboard.core.util;

import org.apache.log4j.spi.ErrorHandler;

/**
 *.
 */
public class APIManagementException extends Exception {

    private ErrorHandler errorHandler;

    /**
     * Get error handler object.
     * @return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * @param message Error message
     * @param cause Error cause
     */
    public APIManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     *
     * @param cause Error cause
     */
    public APIManagementException(Throwable cause) {
        super(cause);
        //this.errorHandler = ExceptionCodes.INTERNAL_ERROR;
    }

    /**
     *
     * @param message Error message
     * @param cause Error cause
     * @param enableSuppression whether you need enable suppression
     * @param writableStackTrace Writable error stack trace.
     */
    protected APIManagementException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     *
     * @param message Error message
     */
    public APIManagementException(String message) {
        super(message);
    }

    /**
     * This is a default constructure where you can pass error code to error DTO.
     * @param message Error message
     * @param code Exception code that need to pass to the error DTO
     */
    public APIManagementException(String message, ErrorHandler code) {
        super(message);
        this.errorHandler = code;
    }

    /**
     * This is a default constructure where you can pass error code to error DTO.
     * @param message Error message
     * @param cause throwable object.
     * @param code Exception code that need to pass to the error DTO
     */
    public APIManagementException(String message, Throwable cause, ErrorHandler code) {
        super(message, cause);
        this.errorHandler = code;
    }

}
