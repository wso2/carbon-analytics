package org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.util;

import feign.Response;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppDeployerServiceStubException;

public class HTTPSClientUtil {
    private static final String PROTOCOL = "https://";

    /**
     * Avoids Instantiation
     */
    private HTTPSClientUtil() {
    }

    /**
     * Generates an HTTPS URL with the given hostAndPort
     *
     * @param hostAndPort Host and Port of the Worker node in {Host}:{Port} format
     * @return HTTPS URL
     */
    private static String generateURL(String hostAndPort) {
        return PROTOCOL + hostAndPort;
    }

    /**
     * Produces a Response after doing a POST request
     *
     * @param hostAndPort Host and Port of the Worker node in {Host}:{Port} format
     * @param username    Username
     * @param password    Password
     * @param payload     Payload
     * @return Feign Response object
     * @throws SiddhiAppDeployerServiceStubException Error occurred within SiddhiAppDeployerServiceStub
     */
    public static Response doPostRequest(String hostAndPort, String username, String password, String payload)
            throws SiddhiAppDeployerServiceStubException {
        return SiddhiAppDeployerFactory.getSiddhiAppDeployerHttpsClient(generateURL(hostAndPort), username, password)
                .doPostRequest(payload);
    }
}
