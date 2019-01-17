package org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer;

import feign.Response;
import feign.RetryableException;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppDeployerServiceStubException;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppsApiHelperException;
import org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.api.SiddhiAppApiHelperService;
import org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.util.HTTPSClientUtil;

public class SiddhiAppApiHelper implements SiddhiAppApiHelperService {
    @Override
    public boolean deploySiddhiApp(String hostAndPort, String username, String password, String siddhiApp, String fileName) throws SiddhiAppsApiHelperException {
        Response response = null;
        try {
            response = HTTPSClientUtil.doPostRequest(hostAndPort, username, password, siddhiApp);
            int status = response.status();
            switch (status) {
                case 201:
                    return true;
                case 400:
                    throw new SiddhiAppsApiHelperException("A validation error occurred during " +
                            "saving the siddhi app '" + fileName + "' on the node '" + hostAndPort + "'", status);
                case 401:
                    throw new SiddhiAppsApiHelperException("Invalid user name or password on the node '" + hostAndPort + "'", status);
                case 409:
                    throw new SiddhiAppsApiHelperException("A Siddhi Application with " +
                            "the given name: '" + fileName + "' already exists in the node '" + hostAndPort + "'", status);
                case 500:
                    throw new SiddhiAppsApiHelperException("Unexpected error occurred during " +
                            "saving the siddhi app '" + fileName + "' on the node '" + hostAndPort + "'", status);
                default:
                    throw new SiddhiAppsApiHelperException("Unexpected status code '" + status + "' received when " +
                            "trying to deploy the siddhi app '" + fileName + "' on node '" + hostAndPort + "'", status);
            }
        } catch (SiddhiAppDeployerServiceStubException e) {
            throw new SiddhiAppsApiHelperException("Failed to deploy siddhi app '" + fileName + "' on the node '" +
                    hostAndPort + "'. ", e);
        } catch (RetryableException e) {
            throw new SiddhiAppsApiHelperException("Cannot connect to the worker node (" + hostAndPort + ") for " +
                    "retrieving status of the siddhi app " + fileName + ".", e);
        } finally {
            closeResponse(response);
        }
    }

    private void closeResponse(Response response) {
        if (response != null) {
            response.close();
        }
    }

}
