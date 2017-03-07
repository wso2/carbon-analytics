package org.wso2.carbon.analytics.indexerservice.exceptions;

/**
 * This exception represents the Exception which might be thrown if there are any issues in the indexers
 */

public class IndexerException extends Exception {

    public IndexerException(String message) {
        super(message);
    }

    public IndexerException(String message, Throwable cause) {
        super(message, cause);
    }
}
