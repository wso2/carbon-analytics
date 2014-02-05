package org.wso2.carbon.analytics.hive.exception;

public class AnalyzerConfigException extends Exception {
    private String errorMessage;

    public AnalyzerConfigException() {
    }

    public AnalyzerConfigException(String message) {
        super(message);
        errorMessage = message;
    }

    public AnalyzerConfigException(String message, Throwable cause) {
        super(message, cause);
        errorMessage = message;
    }

    public AnalyzerConfigException(Throwable cause) {
        super(cause);
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
