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
import org.apache.thrift.TException;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentAuthenticatorException;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;

/**
 * Authenticates all data publishers
 */
public class ThriftAgentAuthenticator extends AgentAuthenticator {

    private static Log log = LogFactory.getLog(ThriftAgentAuthenticator.class);

    public ThriftAgentAuthenticator(GenericKeyedObjectPool secureTransportPool) {
        super(secureTransportPool);
    }

    @Override
    protected String connect(Object client, String userName, String password)
            throws AuthenticationException, AgentAuthenticatorException {
        try {
            return ((ThriftSecureEventTransmissionService.Client) client).connect(userName, password);
        } catch (ThriftAuthenticationException e) {
            throw new AuthenticationException("Thrift Authentication Exception", e);
        } catch (TException e) {
            throw new AgentAuthenticatorException("Thrift exception", e);
        }
    }

    @Override
    protected void disconnect(Object client, String sessionId) throws AgentAuthenticatorException {
        try {
            ((ThriftSecureEventTransmissionService.Client) client).disconnect(sessionId);
        } catch (TException e) {
            throw new AgentAuthenticatorException("Thrift Exception", e);
        }
    }
}
