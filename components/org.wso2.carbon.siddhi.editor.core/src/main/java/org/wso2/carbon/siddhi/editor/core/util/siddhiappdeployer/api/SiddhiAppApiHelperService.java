package org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.api;

import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppsApiHelperException;

public interface SiddhiAppApiHelperService {

    boolean deploySiddhiApp(String nodeUrl, String username, String password, String siddhiApp, String fileName) throws SiddhiAppsApiHelperException;
}
