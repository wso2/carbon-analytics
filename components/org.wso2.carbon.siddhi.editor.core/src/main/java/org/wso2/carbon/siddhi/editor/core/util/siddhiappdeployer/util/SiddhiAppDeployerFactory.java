package org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.util;

import org.wso2.carbon.siddhi.editor.core.internal.EditorDataHolder;

/**
 * Factory that is used to produce a HTTPS client for calling a Worker
 */
public class SiddhiAppDeployerFactory {
    private static final int CLIENT_CONNECTION_TIMEOUT = 5000;
    private static final int CLIENT_READ_TIMEOUT = 5000;

    /**
     * Returns an HTTPS client for deploying Siddhi apps to the Worker
     *
     * @param httpsUrl HTTPS URL of the Worker
     * @param username Username
     * @param password Password
     * @return SiddhiAppDeployerServiceStub instance which functions as the HTTPS client
     */
    public static SiddhiAppDeployerServiceStub getSiddhiAppDeployerHttpsClient(String httpsUrl, String username,
                                                                               String password) {
        return EditorDataHolder.getInstance().getClientBuilderService().build(username, password, CLIENT_CONNECTION_TIMEOUT,
                CLIENT_READ_TIMEOUT, SiddhiAppDeployerServiceStub.class, httpsUrl);
    }
}
