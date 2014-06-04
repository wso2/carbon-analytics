package org.wso2.carbon.databridge.datasink.cassandra.subscriber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.*;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.datasink.cassandra.utils.RegistryAccess;
import org.wso2.carbon.databridge.datasink.cassandra.internal.util.ServiceHolder;
import org.wso2.carbon.databridge.persistence.cassandra.datastore.ClusterFactory;
import org.wso2.carbon.databridge.streamdefn.registry.util.RegistryStreamDefinitionStoreUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.List;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public class BAMEventSubscriber implements AgentCallback {
    private Log log = LogFactory.getLog(BAMEventSubscriber.class);

    @Override
    public void definedStream(StreamDefinition streamDefinition, int tenantId) {

        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            RegistryAccess.saveIndexDefinition(streamDefinition, streamDefinition.getIndexDefinition(), registry);
            //invalidate the stream cache. This means column families for a given stream id should be inspected for
            //updates
            ServiceHolder.getCassandraConnector().invalidateStreamCache(streamDefinition.getStreamId());
        } catch (RegistryException e) {
            log.error("Error in defining Stream Definition " + streamDefinition.getName() + ":" +
                    streamDefinition.getVersion(), e);
        }
    }


    @Override
    public void removeStream(StreamDefinition streamDefinition, int tenantId) {
        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            registry.delete(RegistryStreamDefinitionStoreUtil.getStreamIndexDefinitionPath(streamDefinition.getName()
                    , streamDefinition.getVersion()));
        } catch (RegistryException e) {
            log.error("Error in deleting Stream Definition " + streamDefinition.getName() + ":" +
                    streamDefinition.getVersion(), e);
        }
    }

    @Override
    public void receive(List<Event> eventList, Credentials credentials) {
        if (System.getProperty("disable.bam.event.storage") == null) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(credentials.getTenantId(), true);
                ServiceHolder.getCassandraConnector().insertEventList(
                        credentials, ClusterFactory.getCluster(credentials), eventList);
                PrivilegedCarbonContext.endTenantFlow();
            } catch (Exception e) {
                String errorMsg = "Error processing event. ";
                log.error(errorMsg, e);
            }
        }
    }
}
