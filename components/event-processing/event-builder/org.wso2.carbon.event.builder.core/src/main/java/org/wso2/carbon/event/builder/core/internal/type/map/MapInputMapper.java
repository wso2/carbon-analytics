/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.type.map;

import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.event.builder.core.config.EventBuilderConfiguration;
import org.wso2.carbon.event.builder.core.config.InputMapper;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.config.InputMappingAttribute;
import org.wso2.carbon.event.builder.core.internal.util.helper.EventBuilderConfigHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapInputMapper implements InputMapper {
    private int[] attributePositionMap = null;
    private EventBuilderConfiguration eventBuilderConfiguration = null;

    public MapInputMapper(EventBuilderConfiguration eventBuilderConfiguration) throws EventBuilderConfigurationException {
        this.eventBuilderConfiguration = eventBuilderConfiguration;
        if (eventBuilderConfiguration != null && eventBuilderConfiguration.getInputMapping() instanceof MapInputMapping) {
            MapInputMapping mapInputMapping = (MapInputMapping) eventBuilderConfiguration.getInputMapping();

            int posCount = 0;
            Map<Integer, Integer> payloadDataMap = new HashMap<Integer, Integer>();
            for (InputMappingAttribute inputMappingAttribute : mapInputMapping.getInputMappingAttributes()) {
                payloadDataMap.put(inputMappingAttribute.getToStreamPosition(), posCount++);
                if (payloadDataMap.get(inputMappingAttribute.getToStreamPosition()) == null) {
                    this.attributePositionMap = null;
                    throw new EventBuilderConfigurationException("Error creating map mapping.");
                }
            }
            this.attributePositionMap = new int[payloadDataMap.size()];
            for (int i = 0; i < attributePositionMap.length; i++) {
                attributePositionMap[i] = payloadDataMap.get(i);
            }
        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj) throws EventBuilderConfigurationException {
        if (attributePositionMap == null) {
            throw new EventBuilderConfigurationException("Input mapping is not available for the current input stream definition:");
        }
        Object[] outObjArray;
        if (obj instanceof Map) {
            Map eventMap = (Map) obj;
            List<Object> mapValues = new ArrayList<Object>(eventMap.values());
            List<Object> outObjList = new ArrayList<Object>();
            for (int i = 0; i < this.attributePositionMap.length; i++) {
                outObjList.add(mapValues.get(this.attributePositionMap[i]));
            }
            outObjArray = outObjList.toArray();
        } else {
            throw new EventBuilderConfigurationException("Received event object is not of type map." + this.getClass() + " cannot convert this event.");
        }

        return outObjArray;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventBuilderConfigurationException {
        throw new UnsupportedOperationException("This feature is not yet supported for MapInputMapping");
    }

    @Override
    public Attribute[] getOutputAttributes() {
        MapInputMapping mapInputMapping = (MapInputMapping) eventBuilderConfiguration.getInputMapping();
        List<InputMappingAttribute> inputMappingAttributes = mapInputMapping.getInputMappingAttributes();
        return EventBuilderConfigHelper.getAttributes(inputMappingAttributes);
    }

}
