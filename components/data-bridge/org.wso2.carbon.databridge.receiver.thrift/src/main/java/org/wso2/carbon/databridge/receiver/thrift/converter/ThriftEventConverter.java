/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.databridge.receiver.thrift.converter;


import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.exception.EventConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * the util class that converts Events and its definitions in to various forms
 */
public final class ThriftEventConverter implements EventConverter {

    public Object[] toObjectArray(ThriftEventBundle thriftEventBundle,
                                  AttributeType[] attributeTypeOrder,
                                  IndexCounter indexCounter) {
        if (attributeTypeOrder != null) {
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case INT:
                        objects[i] = thriftEventBundle.getIntAttributeList().get(indexCounter.getIntCount());
                        indexCounter.incrementIntCount();
                        break;
                    case LONG:
                        objects[i] = thriftEventBundle.getLongAttributeList().get(indexCounter.getLongCount());
                        indexCounter.incrementLongCount();
                        break;
                    case STRING:
                        String stringValue = thriftEventBundle.getStringAttributeList().get(indexCounter.getStringCount());
                        if (stringValue.equals(EventDefinitionConverterUtils.NULL_STRING)) {
                            objects[i] = null;
                        } else {
                            objects[i] = stringValue;
                        }
                        indexCounter.incrementStringCount();
                        break;
                    case DOUBLE:
                        objects[i] = thriftEventBundle.getDoubleAttributeList().get(indexCounter.getDoubleCount());
                        indexCounter.incrementDoubleCount();
                        break;
                    case FLOAT:
                        objects[i] = thriftEventBundle.getDoubleAttributeList().get(indexCounter.getDoubleCount()).floatValue();
                        indexCounter.incrementDoubleCount();
                        break;
                    case BOOL:
                        objects[i] = thriftEventBundle.getBoolAttributeList().get(indexCounter.getBoolCount());
                        indexCounter.incrementBoolCount();
                        break;
                }
            }
            return objects;
        } else {
            return null;
        }
    }

    public List<Event> toEventList(Object eventBundle,
                                   StreamTypeHolder streamTypeHolder) {
        if (eventBundle instanceof ThriftEventBundle) {
            return createEventList((ThriftEventBundle) eventBundle, streamTypeHolder);
        } else {
            throw new EventConversionException("Wrong type of event received " + eventBundle.getClass());
        }

    }

    @Override
    public int getSize(Object eventBundle) {
        if (eventBundle instanceof ThriftEventBundle) {
            ThriftEventBundle thriftEventBundle = (ThriftEventBundle) eventBundle;
            int eventBundleSize = 0;
            //arbitray data
            if (thriftEventBundle.isSetArbitraryDataMapMap()) {
                Set<Map.Entry<Integer, Map<String, String>>> arbitraryDataMap = thriftEventBundle.getArbitraryDataMapMap().entrySet();
                for (Map.Entry<Integer, Map<String, String>> arbitraryData : arbitraryDataMap) {
                    eventBundleSize += DataBridgeCommonsUtils.getSize(arbitraryData.getValue());
                }
                eventBundleSize += arbitraryDataMap.size() * 4; // 4 byte per integer
                eventBundleSize += arbitraryDataMap.size() * DataBridgeCommonsUtils.getReferenceSize() * 2; //for the reference
            }
            eventBundleSize += thriftEventBundle.getBoolAttributeListSize();  //1 byte per boolean field
            eventBundleSize += thriftEventBundle.getBoolAttributeListSize() * DataBridgeCommonsUtils.getReferenceSize(); // for each reference
            eventBundleSize += thriftEventBundle.getDoubleAttributeListSize() * 8; //8 bytes per double field
            eventBundleSize += thriftEventBundle.getDoubleAttributeListSize() * DataBridgeCommonsUtils.getReferenceSize(); // for each double reference.
            eventBundleSize += thriftEventBundle.getIntAttributeListSize() * 4; // 4 bytes per int field
            eventBundleSize += thriftEventBundle.getIntAttributeListSize() * DataBridgeCommonsUtils.getReferenceSize(); // for each int reference
            eventBundleSize += thriftEventBundle.getLongAttributeListSize() * 8; // 8 bytes per long field
            eventBundleSize += thriftEventBundle.getLongAttributeListSize() * DataBridgeCommonsUtils.getReferenceSize(); // for each long reference
            for (String aStringField : thriftEventBundle.getStringAttributeList()) {
                eventBundleSize += aStringField.getBytes().length;
            }
            eventBundleSize += thriftEventBundle.getStringAttributeListSize() * DataBridgeCommonsUtils.getReferenceSize(); // for each string reference
            eventBundleSize += 4; //for eventNum field
            eventBundleSize += DataBridgeCommonsUtils.getSize(thriftEventBundle.getSessionId());
            eventBundleSize += 7 * DataBridgeCommonsUtils.getReferenceSize(); // for the references of list and string attributes in thrift event bundle
            return eventBundleSize;
        } else {
            throw new EventConversionException("Wrong type of event received " + eventBundle.getClass());
        }
    }

    @Override
    public int getNumberOfEvents(Object eventBundle) {
        if (eventBundle instanceof ThriftEventBundle) {
            return ((ThriftEventBundle) eventBundle).getEventNum();
        } else {
            throw new EventConversionException("Wrong type event relieved " + eventBundle.getClass());
        }
    }

    private List<Event> createEventList(ThriftEventBundle thriftEventBundle,
                                        StreamTypeHolder streamTypeHolder) {

        IndexCounter indexCounter = new IndexCounter();
        List<Event> eventList = new ArrayList<>(thriftEventBundle.getEventNum());
        String streamId = null;
        try {
            for (int i = 0; i < thriftEventBundle.getEventNum(); i++) {
                Event event = new Event();
                streamId = thriftEventBundle.getStringAttributeList().get(indexCounter.getStringCount());
                indexCounter.incrementStringCount();
                event.setStreamId(streamId);
                long timeStamp = thriftEventBundle.getLongAttributeList().get(indexCounter.getLongCount());
                indexCounter.incrementLongCount();
                event.setTimeStamp(timeStamp);
                AttributeType[][] attributeTypeOrder = streamTypeHolder.getDataType(streamId);
                if (attributeTypeOrder == null) {
                    streamTypeHolder.reloadStreamTypeHolder();
                    attributeTypeOrder = streamTypeHolder.getDataType(streamId);
                    if (attributeTypeOrder == null) {
                        throw new EventConversionException("No StreamDefinition for streamId " + streamId + " present in cache ");
                    }
                }
                event.setMetaData(this.toObjectArray(thriftEventBundle, attributeTypeOrder[0], indexCounter));
                event.setCorrelationData(this.toObjectArray(thriftEventBundle, attributeTypeOrder[1], indexCounter));
                event.setPayloadData(this.toObjectArray(thriftEventBundle, attributeTypeOrder[2], indexCounter));
                if (thriftEventBundle.isSetArbitraryDataMapMap()) {
                    Map<String, String> arbitraryData = thriftEventBundle.getArbitraryDataMapMap().get(i);
                    if (null != arbitraryData) {
                        event.setArbitraryDataMap(arbitraryData);
                    }
                }
                eventList.add(event);
            }
        } catch (RuntimeException re) {
            throw new EventConversionException("Error when converting " + streamId + " of event bundle with events " + thriftEventBundle.getEventNum(), re);
        }
        return eventList;
    }

}
