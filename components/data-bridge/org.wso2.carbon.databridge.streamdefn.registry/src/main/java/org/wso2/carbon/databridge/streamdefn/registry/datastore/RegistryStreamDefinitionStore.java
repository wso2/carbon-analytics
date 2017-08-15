/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.streamdefn.registry.datastore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.commons.utils.IndexDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.streamdefn.registry.internal.ServiceHolder;
import org.wso2.carbon.databridge.streamdefn.registry.util.RegistryStreamDefinitionStoreUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The in memory implementation of the Event Stream definition Store
 */
public class RegistryStreamDefinitionStore extends
        AbstractStreamDefinitionStore {
    private Log log = LogFactory.getLog(RegistryStreamDefinitionStore.class);


    public StreamDefinition getStreamDefinitionFromStore(String name, String version, int tenantId)
            throws StreamDefinitionStoreException {

        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version))) {
                Resource resource = registry.get(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version));
                Object content = resource.getContent();
                if (content != null) {
                    StreamDefinition streamDefinition = EventDefinitionConverterUtils.
                            convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));

                    if (registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamIndexDefinitionPath(name, version))) {
                        Resource indexResource = registry.get(RegistryStreamDefinitionStoreUtil.getStreamIndexDefinitionPath(name, version));
                        streamDefinition.setIndexDefinition(IndexDefinitionConverterUtils.
                                getIndexDefinition(RegistryUtils.decodeBytes((byte[]) indexResource.getContent())));
                    }
                    return streamDefinition;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error in getting Stream Definition " + name + ":" + version, e);
            throw new StreamDefinitionStoreException("Error in getting Stream Definition " + name + ":" + version, e);
        }
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String streamId, int tenantId)
            throws StreamDefinitionStoreException {

        return getStreamDefinitionFromStore(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), tenantId);

    }

    @Override
    public boolean removeStreamDefinition(String name, String version, int tenantId) {

        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            registry.delete(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version));
            return !registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(name, version));
        } catch (RegistryException e) {
            log.error("Error in deleting Stream Definition " + name + ":" + version);
        }


        return false;
    }

    @Override
    public void saveStreamDefinitionToStore(StreamDefinition streamDefinition, int tenantId)
            throws StreamDefinitionStoreException {
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            Resource resource = registry.newResource();
            resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
            resource.setMediaType("application/json");
            registry.put(RegistryStreamDefinitionStoreUtil.getStreamDefinitionPath(streamDefinition.
                    getName(), streamDefinition.getVersion()), resource);
            log.info("Stream definition added to registry successfully : " + streamDefinition.getStreamId());
        } catch (RegistryException e) {
            log.error("Error in saving Stream Definition " + streamDefinition, e);
            throw new StreamDefinitionStoreException("Error in saving Stream Definition " + streamDefinition, e);
        }

    }

    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore(int tenantId) {
        ConcurrentHashMap<String, StreamDefinition> map = new ConcurrentHashMap<String, StreamDefinition>();

        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);

            if (!registry.resourceExists(RegistryStreamDefinitionStoreUtil.getStreamDefinitionStorePath())) {
                registry.put(RegistryStreamDefinitionStoreUtil.getStreamDefinitionStorePath(), registry.newCollection());
            } else {
                org.wso2.carbon.registry.core.Collection collection =
                        (org.wso2.carbon.registry.core.Collection) registry.get(RegistryStreamDefinitionStoreUtil.
                                getStreamDefinitionStorePath());
                for (String streamNameCollection : collection.getChildren()) {

                    org.wso2.carbon.registry.core.Collection innerCollection =
                            (org.wso2.carbon.registry.core.Collection) registry.get(streamNameCollection);
                    for (String streamVersionCollection : innerCollection.getChildren()) {

                        Resource resource = (Resource) registry.get(streamVersionCollection);
                        try {
                            StreamDefinition streamDefinition = EventDefinitionConverterUtils
                                    .convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                            map.put(streamDefinition.getStreamId(), streamDefinition);
                        } catch (Throwable e) {
                            log.error("Error in retrieving streamDefinition from the resource at "
                                    + resource.getPath(), e);
                        }
                    }
                }
            }

        } catch (RegistryException e) {
            log.error("Error in retrieving streamDefinitions from the registry", e);
        }

        return map.values();
    }


}
