package org.wso2.carbon.business.rules.core.api;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T14:26:28.099Z")
public class NotFoundException extends ApiException {
    private int code;

    public NotFoundException(int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
