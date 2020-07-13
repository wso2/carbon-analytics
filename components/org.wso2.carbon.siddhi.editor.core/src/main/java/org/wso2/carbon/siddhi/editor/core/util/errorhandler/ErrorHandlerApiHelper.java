/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.errorhandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.carbon.siddhi.editor.core.exception.ErrorHandlerServiceStubException;
import org.wso2.carbon.siddhi.editor.core.util.errorhandler.api.ErrorHandlerApiHelperService;
import org.wso2.carbon.siddhi.editor.core.util.errorhandler.util.HTTPSClientUtil;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Consists of a method for the deployment feature for the exposed Siddhi App Api.
 */
public class ErrorHandlerApiHelper implements ErrorHandlerApiHelperService {

    // TODO remove this sample implementation when done
//    public boolean deploySiddhiApp(String hostAndPort, String username, String password, String siddhiApp,
//                                   String fileName) throws SiddhiAppDeployerServiceStubException {
//
//        Response response = null;
//        try {
//            response = HTTPSClientUtil.doPutRequest(hostAndPort, username, password, siddhiApp);
//            int status = response.status();
//            switch (status) {
//                case 200:
//                    return true;
//                case 201:
//                    return true;
//                case 400:
//                    throw new SiddhiAppDeployerServiceStubException("Status code " + status +
//                            " received. A validation error occurred during " +
//                            "saving the siddhi app '" + fileName + "' on the node '" + hostAndPort + "'");
//                case 401:
//                    throw new SiddhiAppDeployerServiceStubException("Status code " + status +
//                            " received. Invalid user name or password on the node '" +
//                            hostAndPort + "' for the user " + username);
//                case 500:
//                    throw new SiddhiAppDeployerServiceStubException("Status code " + status +
//                            " received. Unexpected error occurred during " +
//                            "saving the siddhi app '" + fileName + "' on the node '" + hostAndPort + "'");
//                default:
//                    throw new SiddhiAppDeployerServiceStubException("Unexpected status code '" + status +
//                            "' received when trying to deploy the siddhi app '" + fileName + "' on node '" +
//                            hostAndPort + "'");
//            }
//        } catch (RetryableException e) {
//            throw new SiddhiAppDeployerServiceStubException("Cannot connect to the worker node (" + hostAndPort + ") " +
//                    "for retrieving status of the siddhi app " + fileName + ".", e);
//        } finally {
//            closeResponse(response);
//        }
//    }

    private void closeResponse(Response response) {

        if (response != null) {
            response.close();
        }
    }

    private String getAsString(InputStream inputStream) throws IOException {
//        InputStream inputStream = HTTPSClientUtil.doGetErrorEntries(hostAndPort, "admin", "admin", "MappingErrorTest").body().asInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        char charArray[] = new char[(int) inputStream.available()];
        inputStreamReader.read(charArray);
        return new String(charArray);
    }

    @Override
    public JsonArray getSiddhiAppList(String hostAndPort, String username, String password)
        throws ErrorHandlerServiceStubException {
        feign.Response response = HTTPSClientUtil.doGetSiddhiAppList(hostAndPort, username, password); // TODO implement
        switch (response.status()) {
            case 200: // TODO cleanup and add other cases
                if (response.body() != null) {
                    try {
                        String responseBody = getAsString(response.body().asInputStream());
                        return new JsonParser().parse(responseBody).getAsJsonArray();
                    } catch (IOException e) {
                        throw new ErrorHandlerServiceStubException("Failed to read the response.", e);
                    }
                }
            default:
                throw new ErrorHandlerServiceStubException("Failed to get error entries. " + response.reason());
        }
    }

    @Override
    public Map<String, String> getStatus(String hostAndPort, String username, String password) throws ErrorHandlerServiceStubException {
        // TODO implement
        return null;
    }

    @Override
    public JsonArray getErrorEntries(String hostAndPort, String username, String password, String siddhiAppName)
        throws ErrorHandlerServiceStubException {
        feign.Response response = HTTPSClientUtil.doGetErrorEntries(hostAndPort, username, password, siddhiAppName);
        switch (response.status()) {
            case 200: // TODO cleanup and add other cases
                if (response.body() != null) {
                    try {
                        String responseBody = getAsString(response.body().asInputStream());
                        return new JsonParser().parse(responseBody).getAsJsonArray();
                    } catch (IOException e) {
                        throw new ErrorHandlerServiceStubException("Failed to read the response.", e);
                    }
                }
            default:
                throw new ErrorHandlerServiceStubException("Failed to get error entries. " + response.reason());
        }
    }

    @Override
    public boolean replay(String hostAndPort, String username, String password, JsonArray payload)
        throws ErrorHandlerServiceStubException {
        feign.Response response = HTTPSClientUtil.doReplay(hostAndPort, username, password, payload.toString());
        switch (response.status()) {
            case 200:
                return true;
            default:
                throw new ErrorHandlerServiceStubException("There were failures during replay." + response.reason());
        }
    }

    @Override
    public Object deleteErroneousEvent(String hostAndPort, String username, String password, String payload) throws ErrorHandlerServiceStubException {
        // TODO implement
        return null;
    }
}
