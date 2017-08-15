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
package org.wso2.carbon.databridge.streamdefn.filesystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.databridge.streamdefn.filesystem.internal.ServiceHolder;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;


import java.util.Collection;

public class FileSystemStreamDefinitionStore extends AbstractStreamDefinitionStore {
    // TODO: 1/30/17 no tenant concept
    private static final Log log = LogFactory.getLog(FileSystemStreamDefinitionStore.class);

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String name, String version)
            throws StreamDefinitionStoreException {
//        boolean tenantFlowStarted = false;
        try {
//            tenantFlowStarted = startTenantFlow(tenantId);
            return ServiceHolder.getEventStreamService().getStreamDefinition(name, version);
        } catch (EventStreamConfigurationException ex) {
            String msg = "Error while loading the stream definition name: " + name + ", version: " + version;
            log.error(msg, ex);
            throw new StreamDefinitionStoreException(msg, ex);
        } /*finally {
            if (tenantFlowStarted) PrivilegedCarbonContext.endTenantFlow();
        }*/
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String streamId)
            throws StreamDefinitionStoreException {
//        boolean tenantFlowStarted = false;
        try {
//            tenantFlowStarted = startTenantFlow(tenantId);
            return ServiceHolder.getEventStreamService().getStreamDefinition(streamId);
        } catch (EventStreamConfigurationException ex) {
            String msg = "Error while loading the stream definition Id: " + streamId;
            log.error(msg + streamId, ex);
            throw new StreamDefinitionStoreException(msg, ex);
        } /*finally {
            if (tenantFlowStarted) PrivilegedCarbonContext.endTenantFlow();
        }*/
    }

    @Override
    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore()
            throws StreamDefinitionStoreException {
//        boolean tenantFlowStarted = false;
        try {
//            tenantFlowStarted = startTenantFlow(tenantId);
            return ServiceHolder.getEventStreamService().getAllStreamDefinitions();
        } catch (EventStreamConfigurationException ex) {
//            String msg = "Error while loading all stream definitions for tenant: " + tenantId;
            String msg = "Error while loading all stream definitions";
            log.error(msg, ex);
            throw new StreamDefinitionStoreException(msg, ex);
        } /*finally {
            if (tenantFlowStarted) PrivilegedCarbonContext.endTenantFlow();
        }*/
    }

    @Override
    public void saveStreamDefinitionToStore(StreamDefinition streamDefinition)
            throws StreamDefinitionStoreException {
//        boolean tenantFlowStarted = false;
        try {
//            tenantFlowStarted = startTenantFlow(tenantId);
            ServiceHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
        } catch (EventStreamConfigurationException ex) {
            String msg = "Error while saving the stream definition: " + streamDefinition;
            log.error(msg, ex);
            throw new StreamDefinitionStoreException(msg, ex);
        } /*finally {
            if (tenantFlowStarted) PrivilegedCarbonContext.endTenantFlow();
        }*/
    }

    @Override
    public boolean removeStreamDefinition(String name, String version) {
//        boolean tenantFlowStarted = false;
        try {
//            tenantFlowStarted = startTenantFlow(tenantId);
            ServiceHolder.getEventStreamService().removeEventStreamDefinition(name, version);
            return true;
        } catch (EventStreamConfigurationException ex) {
            String msg = "Error while removing the stream definition name :" + name + ", version : " + version;
            log.error(msg, ex);
            return false;
        } /*finally {
            if (tenantFlowStarted) PrivilegedCarbonContext.endTenantFlow();
        }*/
    }

    /*private boolean startTenantFlow(int tenantId) {
        int currentTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (currentTenantId == MultitenantConstants.INVALID_TENANT_ID ||
                (currentTenantId == MultitenantConstants.SUPER_TENANT_ID && currentTenantId != tenantId)) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            return true;
        }
        return false;
    }*/
}
