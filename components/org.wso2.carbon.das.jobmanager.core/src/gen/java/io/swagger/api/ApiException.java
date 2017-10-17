package io.swagger.api;

@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class ApiException extends Exception {
    private int code;

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
