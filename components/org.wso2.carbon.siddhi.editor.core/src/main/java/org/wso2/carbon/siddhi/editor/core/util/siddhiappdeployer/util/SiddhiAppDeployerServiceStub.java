package org.wso2.carbon.siddhi.editor.core.util.siddhiappdeployer.util;

import feign.Headers;
import feign.RequestLine;
import feign.Response;
import org.wso2.carbon.siddhi.editor.core.exception.SiddhiAppDeployerServiceStubException;

/**
 * Feign client for sending requests
 */
public interface SiddhiAppDeployerServiceStub {
    @RequestLine("POST /siddhi-apps")
    @Headers("Content-Type: text/plain; charset=utf-8")
    Response doPostRequest(String payload) throws SiddhiAppDeployerServiceStubException;
}
