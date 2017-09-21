package org.wso2.carbon.business.rules.core.internal.exceptions;

/**
 * Created by minudika on 19/9/17.
 */
public class BusinessRulesDatasourceException extends Exception{
    public BusinessRulesDatasourceException(String message) {
        super(message);
    }

    public BusinessRulesDatasourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessRulesDatasourceException(Throwable cause) {
        super(cause);
    }
}
