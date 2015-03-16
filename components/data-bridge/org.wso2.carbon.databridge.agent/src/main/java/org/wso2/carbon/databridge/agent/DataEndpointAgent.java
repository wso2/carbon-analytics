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

package org.wso2.carbon.databridge.agent;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.agent.client.AbstractClientPoolFactory;
import org.wso2.carbon.databridge.agent.client.AbstractSecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.client.ClientPool;
import org.wso2.carbon.databridge.agent.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.endpoint.DataEndpoint;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * One agent is created for a specific data endpoint type,and this has the resources such as transport pool, etc
 * which are shared by all the data publishers created for the endpoint type.
 */

public class DataEndpointAgent {

    private ArrayList<DataPublisher> dataPublishers = new ArrayList<DataPublisher>();

    private GenericKeyedObjectPool transportPool;

    private GenericKeyedObjectPool securedTransportPool;

    private AgentConfiguration agentConfiguration;

    public DataEndpointAgent(AgentConfiguration agentConfiguration)
            throws DataEndpointAgentConfigurationException {
        this.agentConfiguration = agentConfiguration;
        initialize();
    }

    private void initialize() throws DataEndpointAgentConfigurationException {
        try {
            DataEndpoint dataEndpoint = (DataEndpoint) (DataEndpointAgent.class.getClassLoader().
                    loadClass(agentConfiguration.getClassName()).newInstance());
            AbstractClientPoolFactory clientPoolFactory = (AbstractClientPoolFactory)
                    (DataEndpointAgent.class.getClassLoader().
                            loadClass(dataEndpoint.getClientPoolFactoryClass()).newInstance());
            AbstractSecureClientPoolFactory secureClientPoolFactory = (AbstractSecureClientPoolFactory)
                    (DataEndpointAgent.class.getClassLoader().
                            loadClass(dataEndpoint.getSecureClientPoolFactoryClass()).
                            getConstructor(String.class, String.class).newInstance(
                            agentConfiguration.getTrustStore(),
                            agentConfiguration.getTrustStorePassword()));
            ClientPool clientPool = new ClientPool();
            this.transportPool = clientPool.getClientPool(
                    clientPoolFactory,
                    agentConfiguration.getMaxTransportPoolSize(),
                    agentConfiguration.getMaxIdleConnections(),
                    true,
                    agentConfiguration.getEvictionTimePeriod(),
                    agentConfiguration.getMinIdleTimeInPool());

            this.securedTransportPool = clientPool.getClientPool(
                    secureClientPoolFactory,
                    agentConfiguration.getSecureMaxTransportPoolSize(),
                    agentConfiguration.getSecureMaxIdleConnections(),
                    true,
                    agentConfiguration.getSecureEvictionTimePeriod(),
                    agentConfiguration.getSecureMinIdleTimeInPool());

        } catch (InstantiationException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool "
                    + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool "
                    + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool "
                    + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool "
                    + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new DataEndpointAgentConfigurationException("Error while creating the client pool "
                    + e.getMessage(), e);
        }
    }

    public void addDataPublisher(DataPublisher dataPublisher) {
        dataPublishers.add(dataPublisher);
    }

    public AgentConfiguration getAgentConfiguration() {
        return agentConfiguration;
    }

    public GenericKeyedObjectPool getTransportPool() {
        return transportPool;
    }

    public GenericKeyedObjectPool getSecuredTransportPool() {
        return securedTransportPool;
    }

    public void shutDown(DataPublisher dataPublisher) {
        dataPublishers.remove(dataPublisher);
    }

    public DataEndpoint getNewDataEndpoint() throws DataEndpointException {
        try {
            return (DataEndpoint) (DataEndpointAgent.class.getClassLoader().
                    loadClass(this.getAgentConfiguration().getClassName()).newInstance());
        } catch (InstantiationException e) {
            throw new DataEndpointException("Error while instantiating the endpoint class for endpoint name " +
                    this.getAgentConfiguration().getDataEndpointName() + ". " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new DataEndpointException("Error while instantiating the endpoint class for endpoint name " +
                    this.getAgentConfiguration().getDataEndpointName() + ". " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new DataEndpointException("Class defined: " + this.getAgentConfiguration().getClassName() +
                    " cannot be found for endpoint name " + this.getAgentConfiguration().getDataEndpointName()
                    + ". " + e.getMessage(), e);
        }
    }

    public void shutDown() throws DataEndpointException {
        if (dataPublishers.isEmpty()) {
            try {
                transportPool.close();
                securedTransportPool.close();
            } catch (Exception e) {
                throw new DataEndpointException("Error while closing the transport pool", e);
            }
        }
    }
}

