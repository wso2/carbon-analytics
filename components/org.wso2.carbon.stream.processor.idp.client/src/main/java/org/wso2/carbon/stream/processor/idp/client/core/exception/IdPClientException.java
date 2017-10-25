package org.wso2.carbon.stream.processor.idp.client.core.exception;

public class IdPClientException extends Exception {
    public IdPClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdPClientException(Throwable cause) {
        super(cause);
    }

    public IdPClientException(String message) {
        super(message);
    }
}