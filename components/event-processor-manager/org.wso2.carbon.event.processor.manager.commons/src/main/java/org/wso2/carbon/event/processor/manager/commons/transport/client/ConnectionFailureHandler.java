package org.wso2.carbon.event.processor.manager.commons.transport.client;

/**
 * Created by sajith on 8/13/15.
 */
public interface ConnectionFailureHandler {
    /**
     * Callback to handle the failure of the connection
     * @param e occurred when the connection is failed.
     */
    public void onConnectionFail(Exception e);
}
