/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.agent.thrift.internal.publisher.authenticator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.thrift.conf.DataPublisherConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentAuthenticatorException;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;

/**
 * Authenticates all data publishers
 */
public abstract class AgentAuthenticator {

    private GenericKeyedObjectPool secureTransportPool;

    private static Log log = LogFactory.getLog(AgentAuthenticator.class);

    public AgentAuthenticator(GenericKeyedObjectPool secureTransportPool) {
        this.secureTransportPool = secureTransportPool;
    }

    public String connect(DataPublisherConfiguration dataPublisherConfiguration)
            throws AuthenticationException, TransportException, AgentException {
        Object client = null;

        try {
            client = secureTransportPool.borrowObject(dataPublisherConfiguration.getPublisherKey());
            return connect(client, dataPublisherConfiguration.getReceiverConfiguration().getUserName(),
                           dataPublisherConfiguration.getReceiverConfiguration().getPassword());
        } catch (AgentAuthenticatorException e) {
            throw new AuthenticationException("Access denied for user " +
                                              dataPublisherConfiguration.getReceiverConfiguration().getUserName() + " to login " +
                                              dataPublisherConfiguration.getPublisherKey(), e);
        } catch (Exception e) {
            throw new AgentException("Cannot borrow client for " + dataPublisherConfiguration.getPublisherKey(), e);
        } finally {
            try {
                secureTransportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
            } catch (Exception e) {
                secureTransportPool.clear(dataPublisherConfiguration.getPublisherKey());
            }
        }


    }

    protected abstract String connect(Object client, String userName, String password)
            throws AuthenticationException, AgentAuthenticatorException;


    public void disconnect(DataPublisherConfiguration dataPublisherConfiguration) {
        Object client = null;

        try {
            client = secureTransportPool.borrowObject(dataPublisherConfiguration.getPublisherKey());
            disconnect(client,dataPublisherConfiguration.getSessionId());
        } catch (Exception e) {
            if(log.isDebugEnabled()){
                log.error("Cannot connect to the server at " + dataPublisherConfiguration.getPublisherKey() + " Authenticator", e);
            }
            log.warn("Cannot connect to the server at " + dataPublisherConfiguration.getPublisherKey() + " Authenticator");
        } finally {
            try {
                secureTransportPool.returnObject(dataPublisherConfiguration.getPublisherKey(), client);
            } catch (Exception e) {
                secureTransportPool.clear(dataPublisherConfiguration.getPublisherKey());
            }
        }

    }

    public GenericKeyedObjectPool getSecureTransportPool() {
        return secureTransportPool;
    }

    protected abstract void disconnect(Object client, String sessionId)
            throws AgentAuthenticatorException;

}
