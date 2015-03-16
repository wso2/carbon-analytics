/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.databridge.agent.endpoint.binary;

import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.endpoint.DataEndpoint;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * This class is Binary transport implementation for the Data Endpoint.
 *
 */
public class BinaryDataEndpoint extends DataEndpoint {

    @Override
    protected String login(Object client, String userName, String password) throws DataEndpointAuthenticationException {
        Socket socket = (Socket) client;
        try {
            return sendAndReceiveResponse(socket, BinaryEventConverter.createBinaryLoginMessage(userName, password),
                    BinaryMessageConstants.LOGIN_OPERATION);
        } catch (Exception e) {
            if (e instanceof DataEndpointAuthenticationException) {
                throw (DataEndpointAuthenticationException) e;
            } else {
                throw new DataEndpointAuthenticationException("Error while trying to login to data receiver :"
                        + socket.getRemoteSocketAddress().toString(), e);
            }
        }
    }

    @Override
    protected void logout(Object client, String sessionId) throws DataEndpointAuthenticationException {
        Socket socket = (Socket) client;
        try {
            sendAndReceiveResponse(socket, BinaryEventConverter.createBinaryLogoutMessage(sessionId),
                    BinaryMessageConstants.LOGOUT_OPERATION);
        } catch (Exception e) {
            if (e instanceof DataEndpointAuthenticationException) {
                throw (DataEndpointAuthenticationException) e;
            } else {
                throw new DataEndpointAuthenticationException("Error while trying to logout to data receiver :"
                        + socket.getRemoteSocketAddress().toString(), e);
            }
        }
    }

    @Override
    protected void send(Object client, List<Event> events) throws DataEndpointException,
            SessionTimeoutException, UndefinedEventTypeException {
        Socket socket = (Socket) client;
        String sessionId = getDataEndpointConfiguration().getSessionId();
        try {
            sendAndReceiveResponse(socket, BinaryEventConverter.createBinaryPublishMessage(events, sessionId),
                    BinaryMessageConstants.PUBLISH_OPERATION);
        } catch (Exception e) {
            if (e instanceof DataEndpointException) {
                throw (DataEndpointException) e;
            } else {
                throw new DataEndpointException("Error while trying to publish events to data receiver :"
                        + socket.getRemoteSocketAddress().toString(), e);
            }
        }
    }

    @Override
    public String getClientPoolFactoryClass() {
        return BinaryClientPoolFactory.class.getCanonicalName();
    }

    @Override
    public String getSecureClientPoolFactoryClass() {
        return BinarySecureClientPoolFactory.class.getCanonicalName();
    }

    private String sendAndReceiveResponse(Socket socket, String message, String operation) throws Exception {
        StringBuilder messageBuilder = new StringBuilder();
        OutputStream outputstream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
        BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        /**
         * Send to receiver to the logout request
         */
        bufferedwriter.write(message);
        bufferedwriter.flush();

        while ((message = bufferedReader.readLine()) != null) {
            messageBuilder.append(message).append("\n");
            if (message.equals(BinaryMessageConstants.END_MESSAGE)) {
                break;
            }
        }
        /**
         * validate the response from receiver
         */
        return processResponse(messageBuilder.toString(), operation);
    }

    private String processResponse(String response, String operation) throws Exception {
        String[] responseLines = response.split("\n");
        if (responseLines.length > 0) {
            if (responseLines[0].equals(BinaryMessageConstants.OK_RESPONSE)) {
                if (operation.equals(BinaryMessageConstants.LOGIN_OPERATION)) {
                    if (responseLines.length == 3) {
                        return responseLines[1].replace(BinaryMessageConstants.SESSION_ID_PREFIX, "");
                    } else {
                        throw new DataEndpointAuthenticationException("Unexpected response received from data receiver;" +
                                " expected sessionId is not existing in the response: " + response);
                    }
                } else {
                    return null;
                }
            } else if (responseLines[0].equals(BinaryMessageConstants.ERROR_RESPONSE)) {
                if (responseLines.length >= 5) {
                    throw (Exception) (BinaryDataEndpoint.class.getClassLoader().
                            loadClass(responseLines[1]).getConstructor(String.class).newInstance(responseLines[2]));
                } else {
                    throw new DataEndpointException("Unexpected error format received from receiver :"
                            + response);
                }
            } else {
                throw new DataEndpointException("Unexpected response received from data receiver : "
                        + response);
            }
        } else {
            throw new DataEndpointException("Unexpected empty response received from data receiver");
        }
    }

}
