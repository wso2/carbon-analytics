/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.tcp.transport;

import org.wso2.carbon.tcp.transport.dto.StreamTypeHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to hold information related to transport streams temporally
 */
public class TransportStreamManager {

    private static final TransportStreamManager instance = new TransportStreamManager();
    private Map<Integer, StreamTypeHolder> tenantStreamAttributeMap;

    private TransportStreamManager() {
        tenantStreamAttributeMap = new HashMap<Integer, StreamTypeHolder>();
    }

    public static TransportStreamManager getInstance() {
        return instance;
    }

    public StreamTypeHolder getStreamTypeHolder(int tenantID) {
        return tenantStreamAttributeMap.get(tenantID);
    }

    public void addStreamDefinition(int tenantId, StreamDefinition streamDefinition){
        StreamTypeHolder streamTypeHolder = tenantStreamAttributeMap.get(tenantId);
        if (streamTypeHolder == null){
            streamTypeHolder = new StreamTypeHolder(tenantId);
            streamTypeHolder.putStreamDefinition(streamDefinition);
            tenantStreamAttributeMap.put(tenantId, streamTypeHolder);
        } else {
            streamTypeHolder.putStreamDefinition(streamDefinition);
        }
    }
}
