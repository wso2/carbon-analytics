/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.SessionTimeoutException;
import org.wso2.carbon.databridge.commons.exception.UndefinedEventTypeException;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.List;

/**
 * Abstract DataReceiver
 */
public abstract class AbstractDataReceiver implements DataBridgeReceiverService {


    @Override
    public String defineStream(String sessionId, String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException,
                   SessionTimeoutException {
        return getDatabridgeReceiver().defineStream(sessionId, streamDefinition);
    }

    @Override
    public String defineStream(String sessionId, String streamDefinition, String indexDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException,
                   SessionTimeoutException {
        return getDatabridgeReceiver().defineStream(sessionId, streamDefinition, indexDefinition);
    }

    @Override
    public String findStreamId(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException {
        return getDatabridgeReceiver().findStreamId(sessionId, streamName, streamVersion);
    }

    @Override
    public boolean deleteStream(String sessionId, String streamId)
            throws SessionTimeoutException {
        return getDatabridgeReceiver().deleteStream(sessionId, streamId);
    }

    @Override
    public boolean deleteStream(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException {
        return getDatabridgeReceiver().deleteStream(sessionId, streamName, streamVersion);
    }

    @Override
    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException {
        getDatabridgeReceiver().publish(eventBundle, sessionId, eventConverter);
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        return getDatabridgeReceiver().login(username, password);
    }

    @Override
    public void logout(String sessionId) throws Exception {
        getDatabridgeReceiver().logout(sessionId);
    }

    @Override
    public StreamDefinition getStreamDefinition(String sessionId, String streamName,
                                                String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException,
                   StreamDefinitionStoreException {
        return getDatabridgeReceiver().getStreamDefinition(sessionId, streamName, streamVersion);
    }

    @Override
    public List<StreamDefinition> getAllStreamDefinitions(String sessionId)
            throws SessionTimeoutException {
        return getDatabridgeReceiver().getAllStreamDefinitions(sessionId);
    }

    @Override
    public void saveStreamDefinition(String sessionId, StreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
                   DifferentStreamDefinitionAlreadyDefinedException {
        getDatabridgeReceiver().saveStreamDefinition(sessionId, streamDefinition);
    }

    @Override
    public DataBridgeConfiguration getInitialConfig() {
        return getDatabridgeReceiver().getInitialConfig();
    }

    protected abstract DataBridgeReceiverService getDatabridgeReceiver();
}
