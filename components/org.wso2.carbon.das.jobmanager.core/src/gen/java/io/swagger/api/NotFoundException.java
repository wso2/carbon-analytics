package io.swagger.api;

@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class NotFoundException extends ApiException {
    private int code;

    public NotFoundException(int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
