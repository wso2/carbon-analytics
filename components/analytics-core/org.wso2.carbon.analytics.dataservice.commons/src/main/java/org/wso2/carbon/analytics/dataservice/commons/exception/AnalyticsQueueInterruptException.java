package org.wso2.carbon.analytics.dataservice.commons.exception;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the exception class where the queue is being interrupted when shutdown hook is triggered.
 */
public class AnalyticsQueueInterruptException extends AnalyticsException {

    private static final long serialVersionUID = 2945719572725443092L;

    public AnalyticsQueueInterruptException(String msg) {
        super(msg);
    }

    public AnalyticsQueueInterruptException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
