/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.event.processor.manager.core;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.processor.manager.core.internal.util.ConfigurationConstants;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 5/26/15.
 */
public class EventManagementUtil {

    public static String constructEventSyncId(int tenantId, String name, Manager.ManagerType type) {
        return tenantId + "/" + type + "/" + name;
    }

    public static String getSyncIdFromDatabridgeStream(org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition) {
        String streamId = streamDefinition.getStreamId();
        String[] streamIdComponents = streamId.split(":");
        if (streamIdComponents.length > 1) {
            return streamIdComponents[0];
        }

        return streamId;
    }

    public static StreamDefinition constructStreamDefinition(String syncId, org.wso2.carbon.databridge.commons.StreamDefinition inStreamDefinition) {

        org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
        streamDefinition.setId(syncId);

        List<Attribute> attributes = new ArrayList<>();
        if (inStreamDefinition.getMetaData() != null) {
            for (Attribute attr : inStreamDefinition.getMetaData()) {
                attributes.add(new Attribute(ConfigurationConstants.PROPERTY_META_PREFIX + attr.getName(), attr.getType()));
            }
        }
        if (inStreamDefinition.getCorrelationData() != null) {
            for (Attribute attr : inStreamDefinition.getCorrelationData()) {
                attributes.add(new Attribute(ConfigurationConstants.PROPERTY_CORRELATION_PREFIX + attr.getName(), attr.getType()));
            }
        }
        if (inStreamDefinition.getPayloadData() != null) {
            attributes.addAll(inStreamDefinition.getPayloadData());
        }
        for (Attribute attr : attributes) {
            streamDefinition.attribute(attr.getName(), org.wso2.siddhi.query.api.definition.Attribute.Type.valueOf(attr.getType().toString()));
        }
        return streamDefinition;
    }

    public static org.wso2.carbon.databridge.commons.StreamDefinition constructDatabridgeStreamDefinition(String syncId, org.wso2.carbon.databridge.commons.StreamDefinition inStreamDefinition) {
        org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition = new org.wso2.carbon.databridge.commons.StreamDefinition(syncId);
        streamDefinition.setMetaData(inStreamDefinition.getMetaData());
        streamDefinition.setCorrelationData(inStreamDefinition.getCorrelationData());
        streamDefinition.setPayloadData(inStreamDefinition.getPayloadData());
        return streamDefinition;
    }

    public static Event getWso2Event(org.wso2.carbon.databridge.commons.StreamDefinition streamDefinition, long timestamp, Object[] data) {
        int metaAttrCount = streamDefinition.getMetaData() != null ? streamDefinition.getMetaData().size() : 0;
        int correlationAttrCount = streamDefinition.getCorrelationData() != null ? streamDefinition.getCorrelationData().size() : 0;
        int payloadAttrCount = streamDefinition.getPayloadData() != null ? streamDefinition.getPayloadData().size() : 0;
        Object[] metaAttrArray = new Object[metaAttrCount];
        Object[] correlationAttrArray = new Object[correlationAttrCount];
        Object[] payloadAttrArray = new Object[payloadAttrCount];
        for (int i = 0; i < data.length; i++) {
            if (i < metaAttrCount) {
                metaAttrArray[i] = data[i];
            } else if (i < metaAttrCount + correlationAttrCount) {
                correlationAttrArray[i - metaAttrCount] = data[i];
            } else {
                payloadAttrArray[i - (metaAttrCount + correlationAttrCount)] = data[i];
            }
        }
        return new Event(streamDefinition.getStreamId(), timestamp, metaAttrArray, correlationAttrArray, payloadAttrArray);
    }
}
