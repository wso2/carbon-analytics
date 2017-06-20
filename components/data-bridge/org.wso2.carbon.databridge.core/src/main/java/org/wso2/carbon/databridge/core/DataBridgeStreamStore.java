/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.databridge.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

/**
 * Class which adds/removes stream definition to InMemoryStreamDefinitionStore
 */
public class DataBridgeStreamStore {

    private static final Log log = LogFactory.getLog(DataBridgeStreamStore.class);

    public void addStreamDefinition(StreamDefinition streamDefinition) {

        InMemoryStreamDefinitionStore inMemoryStreamDefinitionStore = DataBridgeServiceValueHolder.
                getStreamDefinitionStore();
        try {
            if (inMemoryStreamDefinitionStore != null) {
                inMemoryStreamDefinitionStore.saveStreamDefinitionToStore(streamDefinition);
            }
        } catch (StreamDefinitionStoreException e) {
            log.error("Exception occurred when adding stream definition : " + streamDefinition.getName() +
                    " to Inmemory Event Store", e);
        }
    }

    public void removeStreamDefinition(String streamName, String streamVersion) {

        InMemoryStreamDefinitionStore inMemoryStreamDefinitionStore = DataBridgeServiceValueHolder.
                getStreamDefinitionStore();
        if(inMemoryStreamDefinitionStore != null){
            inMemoryStreamDefinitionStore.removeStreamDefinition(streamName, streamVersion);
        }
    }

}
