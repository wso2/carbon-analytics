package org.wso2.carbon.stream.processor.statistic.api;

@javax.annotation.Generated(value = "org.wso2.carbon.status.dashboard.core..codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T05:58:14.415Z")
public class NotFoundException extends ApiException {
    private int code;
    public NotFoundException (int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
