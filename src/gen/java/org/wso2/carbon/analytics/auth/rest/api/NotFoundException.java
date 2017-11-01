package org.wso2.carbon.analytics.auth.rest.api;

/**
 * Not Found Exception.
 */
public class NotFoundException extends ApiException {
    private int code;

    public NotFoundException(int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
