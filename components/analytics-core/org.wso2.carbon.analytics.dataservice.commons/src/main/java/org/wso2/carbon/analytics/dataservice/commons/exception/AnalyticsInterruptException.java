package org.wso2.carbon.analytics.dataservice.commons.exception;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the exception class where the queue is being interrupted when shutdown hook is triggered.
 */
public class AnalyticsInterruptException extends AnalyticsException {

    private static final long serialVersionUID = 2945719572725443092L;

    public AnalyticsInterruptException(String msg) {
        super(msg);
    }

    public AnalyticsInterruptException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
