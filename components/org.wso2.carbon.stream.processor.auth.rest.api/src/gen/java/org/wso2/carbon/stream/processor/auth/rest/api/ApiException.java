package org.wso2.carbon.stream.processor.auth.rest.api;

public class ApiException extends Exception{
    private int code;
    public ApiException (int code, String msg) {
        super(msg);
        this.code = code;
    }
}
