package org.wso2.carbon.stream.processor.statistics.internal.exception;

/**
 * customized exception class for metrics dashboard related configurations
 */
public class MetricsConfigException extends Exception{

    public MetricsConfigException(String message) {
        super(message);
    }

    public MetricsConfigException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MetricsConfigException(Throwable throwable) {
        super(throwable);
    }
}