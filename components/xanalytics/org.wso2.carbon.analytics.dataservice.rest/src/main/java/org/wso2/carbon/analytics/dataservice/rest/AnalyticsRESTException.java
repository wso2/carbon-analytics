package org.wso2.carbon.analytics.dataservice.rest;

public class AnalyticsRESTException extends Exception {

    private static final long serialVersionUID = 1L;

	public AnalyticsRESTException(String message) {
        super(message);
    }

    public AnalyticsRESTException(Throwable e) {
        super(e);
    }

    public AnalyticsRESTException(String message, Throwable e) {
        super(message, e);
    }
}
