package org.wso2.carbon.databridge.receiver.restapi.utils;

import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.restapi.RESTAPIConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RESTUtils {

    public static Map<String, StreamDefinition> streamDefinitionMap = new HashMap<String, StreamDefinition>();

    public static Credentials extractAuthHeaders(HttpServletRequest request) {
        String authzHeader = request.getHeader("Authorization");
        return extractAuthHeaders(authzHeader);
    }

    public static Credentials extractAuthHeaders(String authHeader) {

        String usernameAndPassword = new String(Base64.decodeBase64(authHeader.substring(6).getBytes()));

        int userNameIndex = usernameAndPassword.indexOf(":");
        String userName=usernameAndPassword.substring(0, userNameIndex);

        return new Credentials(userName,
                usernameAndPassword.substring(userNameIndex + 1), MultitenantUtils.getTenantDomain(userName));
    }

    public static boolean authenticate(HttpServletRequest request) throws AuthenticationException {
        Credentials credentials = extractAuthHeaders(request);
        DataBridgeReceiverService dataBridgeReceiverService =
                (DataBridgeReceiverService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getOSGiService(DataBridgeReceiverService.class);
        String sessionId = dataBridgeReceiverService.login(credentials.getUsername(), credentials.getPassword());
        HttpSession session = request.getSession(true);
        session.setAttribute(RESTAPIConstants.SESSION_ID, sessionId);
        return true;
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object attribute = session.getAttribute(RESTAPIConstants.SESSION_ID);
        return (attribute != null);
    }

    public static String getSessionId(HttpServletRequest request) throws AuthenticationException {
        if (!isAuthenticated(request)) {
            authenticate(request);
        }
        return (String) request.getSession().getAttribute(RESTAPIConstants.SESSION_ID);
    }

    public static StreamDefinition getStreamDefinition(String sessionId, Credentials credentials,
                                                       String streamName, String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException, StreamDefinitionStoreException {
        StreamDefinition streamDefinition = null;
        String key = credentials.getUsername() + ":" + streamName + ":" + streamVersion;

        if(streamDefinitionMap.containsKey(key)) {
            return streamDefinitionMap.get(key);
        } else {
            DataBridgeReceiverService dataBridgeReceiverService =
                    (DataBridgeReceiverService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(DataBridgeReceiverService.class);
            streamDefinition = dataBridgeReceiverService.getStreamDefinition(sessionId, streamName, streamVersion);
            streamDefinitionMap.put(key, streamDefinition);
            return streamDefinition;
        }
    }

    public static void deleteStreamDefinition(Credentials credentials, String streamName, String streamVersion) {
        String key = credentials.getUsername() + ":" + streamName + ":" + streamVersion;
        streamDefinitionMap.remove(key);
    }
}
