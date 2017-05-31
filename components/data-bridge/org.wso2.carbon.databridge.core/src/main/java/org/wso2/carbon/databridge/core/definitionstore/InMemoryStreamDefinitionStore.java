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

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The in memory implementation of the Event Stream definition Store
 */
public class InMemoryStreamDefinitionStore extends
                                           AbstractStreamDefinitionStore {

    private ConcurrentHashMap<String, StreamDefinition> streamDefinitionStore = new ConcurrentHashMap<String, StreamDefinition>();

    @Override
    public boolean removeStreamDefinition(String name, String version) {
        if (null != streamDefinitionStore.remove(DataBridgeCommonsUtils.generateStreamId(name, version))) {
            return true;
        }
        return false;
    }

    @Override
    public void saveStreamDefinitionToStore(StreamDefinition streamDefinition)
            throws StreamDefinitionStoreException {

        streamDefinitionStore.put(streamDefinition.getStreamId(), streamDefinition);
    }


    @Override
    public StreamDefinition getStreamDefinitionFromStore(String name, String version)
            throws StreamDefinitionStoreException {
        return getStreamDefinition(DataBridgeCommonsUtils.generateStreamId(name, version));
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String streamId)
            throws StreamDefinitionStoreException {
        return streamDefinitionStore.get(streamId);
    }

    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore() {
        if (streamDefinitionStore != null) {
            return streamDefinitionStore.values();
        }
        return null;
    }

}
