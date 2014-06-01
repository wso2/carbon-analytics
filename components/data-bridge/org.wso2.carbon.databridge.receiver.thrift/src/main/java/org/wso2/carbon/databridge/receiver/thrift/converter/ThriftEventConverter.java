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

package org.wso2.carbon.databridge.receiver.thrift.converter;


import com.google.gson.Gson;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.thrift.data.ThriftEventBundle;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.EventConverter;
import org.wso2.carbon.databridge.core.StreamTypeHolder;
import org.wso2.carbon.databridge.core.exception.EventConversionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * the util class that converts Events and its definitions in to various forms
 */
public final class ThriftEventConverter implements EventConverter {
    private static Gson gson = new Gson();

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
                        if (stringValue.equals(EventDefinitionConverterUtils.nullString)) {
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
            throw new EventConversionException("Wrong type event relieved " + eventBundle.getClass());
        }

    }

    private List<Event> createEventList(ThriftEventBundle thriftEventBundle,
                                        StreamTypeHolder streamTypeHolder) {

        IndexCounter indexCounter = new IndexCounter();
        List<Event> eventList = new ArrayList<Event>(thriftEventBundle.getEventNum());
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
                    throw new EventConversionException("No StreamDefinition for streamId " + streamId + " present in cache ");
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
