/*
 * Copyright 2012 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.databridge.streamdefn.cassandra.datastore;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.ClusterFactory;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.ServiceHolder;

import java.util.Collection;

/**
 * Cassandra based Event Stream Definition store implementation
 */
public class CassandraStreamDefinitionStore extends AbstractStreamDefinitionStore {

    Logger log = Logger.getLogger(CassandraStreamDefinitionStore.class);


    public CassandraStreamDefinitionStore() {
    }


    @Override
    protected boolean removeStreamDefinition(Credentials credentials, String name, String version) {
        try {
            return ServiceHolder.getCassandraConnector()
                    .deleteStreamDefinitionFromStore(ClusterFactory.getCluster(credentials),
                            DataBridgeCommonsUtils.generateStreamId(name, version));
        } catch (StreamDefinitionStoreException e) {
            return false;
        }
    }

    @Override
    protected void saveStreamDefinitionToStore(Credentials credentials, StreamDefinition streamDefinition) throws StreamDefinitionStoreException {
        StreamValidatorUtil streamValidatorUtil = StreamValidatorUtil.getInstance();
        try {
            if(streamValidatorUtil.isPersistedStream(streamDefinition.getName(), streamDefinition.getVersion())) {
                ServiceHolder.getCassandraConnector().saveStreamDefinitionToStore(ClusterFactory.getCluster(credentials), streamDefinition);
            }
        } catch (DataBridgeException e) {
            log.error(e.getErrorMessage());
        }
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(Credentials credentials, String name, String version) throws StreamDefinitionStoreException {
        return ServiceHolder.getCassandraConnector().getStreamDefinitionFromCassandra(
                ClusterFactory.getCluster(credentials),
                DataBridgeCommonsUtils.generateStreamId(name, version));

    }

    @Override
    protected StreamDefinition getStreamDefinitionFromStore(
            Credentials credentials, String streamId) throws StreamDefinitionStoreException {
        return ServiceHolder.getCassandraConnector().getStreamDefinitionFromCassandra(
                ClusterFactory.getCluster(credentials), streamId);
    }

    @Override
    protected Collection<StreamDefinition> getAllStreamDefinitionsFromStore(Credentials credentials) throws
            StreamDefinitionStoreException {
        return ServiceHolder.getCassandraConnector().getAllStreamDefinitionFromStore(
                ClusterFactory.getCluster(credentials));
    }


}
