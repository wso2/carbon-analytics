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

package org.wso2.carbon.event.formatter.core.internal.type.map;

import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.OutputMapper;
import org.wso2.carbon.event.formatter.core.internal.config.EventOutputProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapOutputOutputMapper implements OutputMapper {

    EventFormatterConfiguration eventFormatterConfiguration = null;
    Map<String, Integer> propertyPositionMap = null;


    public MapOutputOutputMapper(EventFormatterConfiguration eventFormatterConfiguration,
                                 Map<String, Integer> propertyPositionMap,
                                 int tenantId) throws
            EventFormatterConfigurationException {
        this.eventFormatterConfiguration = eventFormatterConfiguration;
        this.propertyPositionMap = propertyPositionMap;
        validateStreamDefinitionWithOutputProperties();

    }

    private void validateStreamDefinitionWithOutputProperties()
            throws EventFormatterConfigurationException {

        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventFormatterConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        Iterator<EventOutputProperty> outputPropertyConfigurationIterator = outputPropertyConfiguration.iterator();
        for (; outputPropertyConfigurationIterator.hasNext(); ) {
            EventOutputProperty outputProperty = outputPropertyConfigurationIterator.next();
            if (!propertyPositionMap.containsKey(outputProperty.getValueOf())) {
                throw new EventFormatterConfigurationException("Property " + outputProperty.getValueOf() + " is not in the input stream definition. ");
            }
        }
    }

    @Override
    public Object convertToMappedInputEvent(Object obj)
            throws EventFormatterConfigurationException {
        Object[] inputObjArray = (Object[]) obj;
        Map<Object, Object> eventMapObject = new TreeMap<Object, Object>();
        MapOutputMapping mapOutputMapping = (MapOutputMapping) eventFormatterConfiguration.getOutputMapping();
        List<EventOutputProperty> outputPropertyConfiguration = mapOutputMapping.getOutputPropertyConfiguration();

        if (outputPropertyConfiguration.size() != 0 && inputObjArray.length > 0) {
            for (EventOutputProperty eventOutputProperty : outputPropertyConfiguration) {
                int position = propertyPositionMap.get(eventOutputProperty.getValueOf());
                eventMapObject.put(eventOutputProperty.getName(), inputObjArray[position]);
            }
        }
        return eventMapObject;
    }

    @Override
    public Object convertToTypedInputEvent(Object obj) throws EventFormatterConfigurationException {
        throw new UnsupportedOperationException("This feature is not yet supported for MapOutputMapping");
    }

}
