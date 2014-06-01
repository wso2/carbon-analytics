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
package org.wso2.carbon.databridge.core.definitionstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Event Stream Definition Store interface
 * Used to persist Event Stream Definitions at the Agent Server
 */
public abstract class AbstractStreamDefinitionStore implements StreamDefinitionStore {

    private Log log = LogFactory.getLog(AbstractStreamDefinitionStore.class);
    private List<StreamAddRemoveListener> streamAddRemoveListenerList = new ArrayList<StreamAddRemoveListener>();

    public StreamDefinition getStreamDefinition(String name,
                                                String version, int tenantId)
            throws StreamDefinitionStoreException {
        return getStreamDefinitionFromStore(name, version, tenantId);
    }

    public StreamDefinition getStreamDefinition(String streamId, int tenantId)
            throws StreamDefinitionStoreException {
        return getStreamDefinitionFromStore(streamId, tenantId);
    }

    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId) {
        try {
            return getAllStreamDefinitionsFromStore(tenantId);
        } catch (StreamDefinitionStoreException e) {
            log.error("Error occured when trying to retrieve definitions. Returning empty list.");
            return new ArrayList<StreamDefinition>();
        }
    }

    public void saveStreamDefinition(StreamDefinition streamDefinition, int tenantId)
            throws DifferentStreamDefinitionAlreadyDefinedException,
            StreamDefinitionStoreException {
        StreamDefinition existingDefinition;
        existingDefinition = getStreamDefinition(streamDefinition.getName(), streamDefinition.getVersion(), tenantId);
        if (existingDefinition == null) {
            saveStreamDefinitionToStore(streamDefinition, tenantId);
            for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                streamAddRemoveListener.streamAdded(tenantId, streamDefinition.getStreamId());
            }
            return;
        }
        if (!existingDefinition.equals(streamDefinition)) {
            throw new DifferentStreamDefinitionAlreadyDefinedException("Cannot define Stream definition:"+EventDefinitionConverterUtils.convertToJson(existingDefinition)+ ", Another Stream with same name and version" +
                    " exist :" + EventDefinitionConverterUtils
                    .convertToJson(existingDefinition));
        }
    }

    public boolean deleteStreamDefinition(String streamName, String streamVersion, int tenantId) {
        if (removeStreamDefinition(streamName, streamVersion, tenantId)) {
            for (StreamAddRemoveListener streamAddRemoveListener : streamAddRemoveListenerList) {
                streamAddRemoveListener.streamRemoved(tenantId, streamName + ":" + streamVersion);
            }
            return true;
        }
        return false;
    }

    @Override
    public void subscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.add(streamAddRemoveListener);
        }
    }

    @Override
    public void unsubscribe(StreamAddRemoveListener streamAddRemoveListener) {
        if (streamAddRemoveListener != null) {
            streamAddRemoveListenerList.remove(streamAddRemoveListener);
        }
    }


    public abstract StreamDefinition getStreamDefinitionFromStore(String name, String version, int tenantId)
            throws StreamDefinitionStoreException;

    public abstract StreamDefinition getStreamDefinitionFromStore(String streamId, int tenantId)
            throws StreamDefinitionStoreException;

    public abstract Collection<StreamDefinition> getAllStreamDefinitionsFromStore(int tenantId)
            throws StreamDefinitionStoreException;

    public abstract void saveStreamDefinitionToStore(StreamDefinition streamDefinition, int tenantId)
            throws StreamDefinitionStoreException;

    public abstract boolean removeStreamDefinition(String name, String version, int tenantId);

}
