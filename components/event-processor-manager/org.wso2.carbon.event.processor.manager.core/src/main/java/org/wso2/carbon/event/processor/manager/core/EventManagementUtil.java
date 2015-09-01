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

    public static StreamDefinition constructStreamDefinition(String syncId, org.wso2.carbon.databridge.commons.StreamDefinition inStreamDefinition){

        org.wso2.siddhi.query.api.definition.StreamDefinition streamDefinition = new org.wso2.siddhi.query.api.definition.StreamDefinition();
        streamDefinition.setId(syncId);

        List<Attribute> attributes = new ArrayList<Attribute>();
        if (inStreamDefinition.getMetaData() != null) {
            for (Attribute attr : inStreamDefinition.getMetaData()) {
                attributes.add(new Attribute("meta_" + attr.getName(), attr.getType()));
            }
        }
        if (inStreamDefinition.getCorrelationData() != null) {
            for (Attribute attr : inStreamDefinition.getCorrelationData()) {
                attributes.add(new Attribute("correlation_" + attr.getName(), attr.getType()));
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
}
