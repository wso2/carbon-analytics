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
import org.wso2.carbon.databridge.core.definitionstore.StreamAddRemoveListener;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.List;

/**
 * this class represents as the interface between the agent server and agent
 * server implementations.
 */
public interface DataBridgeReceiverService {


    public String defineStream(String sessionId, String streamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException, SessionTimeoutException;

    public String defineStream(String sessionId, String streamDefinition, String indexDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedStreamDefinitionException, SessionTimeoutException;

    public String findStreamId(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException;

    public boolean deleteStream(String sessionId, String streamId)
            throws SessionTimeoutException;

    public boolean deleteStream(String sessionId, String streamName, String streamVersion)
            throws SessionTimeoutException;

    public void publish(Object eventBundle, String sessionId, EventConverter eventConverter)
            throws UndefinedEventTypeException, SessionTimeoutException;

    public StreamDefinition getStreamDefinition(String sessionId, String streamName,
                                                String streamVersion)
            throws SessionTimeoutException, StreamDefinitionNotFoundException,
                   StreamDefinitionStoreException;

    public List<StreamDefinition> getAllStreamDefinitions(String sessionId)
            throws SessionTimeoutException;

    public void saveStreamDefinition(String sessionId, StreamDefinition streamDefinition)
            throws SessionTimeoutException, StreamDefinitionStoreException,
                   DifferentStreamDefinitionAlreadyDefinedException;


    public String login(String username, String password) throws AuthenticationException;

    public void logout(String sessionId) throws Exception;

    public DataBridgeConfiguration getInitialConfig();

    public void subscribe(StreamAddRemoveListener streamAddRemoveListener);

    public void unsubscribe(StreamAddRemoveListener streamAddRemoveListener);
}




