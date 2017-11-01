package org.wso2.carbon.analytics.auth.rest.api;

/**
 * API Exception class.
 */
public class ApiException extends Exception {
    private int code;

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
