package org.wso2.carbon.stream.processor.statistic.api;

@javax.annotation.Generated(value = "org.wso2.carbon.status.dashboard.core..codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T05:58:14.415Z")
public class ApiException extends Exception{
    private int code;
    public ApiException (int code, String msg) {
        super(msg);
        this.code = code;
    }
}
