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
package org.wso2.carbon.databridge.core;

import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.internal.EventDispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event stream data type holder
 */
public class StreamTypeHolder {
    private Map<String, StreamAttributeComposite> attributeCompositeMap = new ConcurrentHashMap<String, StreamAttributeComposite>();
    private EventDispatcher eventDispatcherCallback;

    public Map<String, StreamAttributeComposite> getAttributeCompositeMap() {
        return attributeCompositeMap;
    }

    public AttributeType[][] getDataType(String streamId) {
        StreamAttributeComposite type = attributeCompositeMap.get(streamId);
        if (null != type) {
            return type.getAttributeTypes();
        }
        return null;
    }

    public void reloadStreamTypeHolder() {
        eventDispatcherCallback.reloadDomainNameStreamTypeHolderCache();
    }

    public StreamAttributeComposite getAttributeComposite(String streamId) {
        return attributeCompositeMap.get(streamId);
    }

    public void putStreamDefinition(StreamDefinition streamDefinition) {
        this.attributeCompositeMap.put(streamDefinition.getStreamId(), new StreamAttributeComposite(streamDefinition));
    }

    public void setEventDispatcherCallback(EventDispatcher eventDispatcherCallback) {
        this.eventDispatcherCallback = eventDispatcherCallback;
    }

}
