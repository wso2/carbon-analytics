/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.databridge.core.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.IOException;
import java.util.List;

/**
 * Databridge utils class
 */
public class DataBridgeUtils {

    private static Log log = LogFactory.getLog(DataBridgeUtils.class);

    public static boolean equals(Event event1, Event event2, StreamDefinition streamDefinition) {
        if (event1 == event2) {
            return true;
        }

        List<Attribute> payloadDefinitions = streamDefinition.getPayloadData();
        List<Attribute> correlationDefinitions = streamDefinition.getCorrelationData();
        List<Attribute> metaDefinitions = streamDefinition.getMetaData();


        try {
            if (!(event1.getStreamId().equals(event2.getStreamId()))) {
                return false;
            }
            if (!(event1.getTimeStamp() == event2.getTimeStamp())) {
                return false;
            }

            if (payloadDefinitions != null) {
                for (int i = 0; i < payloadDefinitions.size(); i++) {
                    Attribute attribute = payloadDefinitions.get(i);
                    if (!compare(event1.getPayloadData()[i], event2.getPayloadData()[i], attribute.getType())) {
                        return false;
                    }

                }
            } else {
                if (!(event1.getPayloadData() == event2.getPayloadData())) {
                    return false;
                }
            }

            if (metaDefinitions != null) {
                for (int i = 0; i < metaDefinitions.size(); i++) {
                    Attribute attribute = metaDefinitions.get(i);
                    if (!compare(event1.getMetaData()[i], event2.getMetaData()[i], attribute.getType())) {
                        return false;
                    }
                }

            } else {
                if (!(event1.getMetaData() == event2.getMetaData())) {
                    return false;
                }
            }

            if (correlationDefinitions != null) {
                for (int i = 0; i < correlationDefinitions.size(); i++) {
                    Attribute attribute = correlationDefinitions.get(i);
                    if (!compare(event1.getCorrelationData()[i], event2.getCorrelationData()[i], attribute.getType())) {
                        return false;
                    }
                }
            } else {
                if (!(event1.getCorrelationData() == event2.getCorrelationData())) {
                    return false;
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    public static boolean compare(Object eventAttr1, Object eventAttr2, AttributeType attributeType)
            throws IOException {
        switch (attributeType) {
            case BOOL: {
                if (eventAttr1 != eventAttr2) {
                    return false;
                }
                break;
            }
            case INT: {

                Integer tempVal1 =
                        (eventAttr1 instanceof Integer) ? (Integer) eventAttr1 : ((Double) eventAttr1).intValue();
                Integer tempVal2 =
                        (eventAttr2 instanceof Integer) ? (Integer) eventAttr2 : ((Double) eventAttr2).intValue();

                if (!tempVal1.equals(tempVal2)) {
                    return false;
                }
                break;
            }
            case DOUBLE: {
                if (Double.compare((Double) eventAttr1, (Double) eventAttr2) != 0) {
                    return false;
                }
                break;
            }
            case FLOAT: {
                if (Float.compare(((Double) eventAttr1).floatValue(), ((Double) eventAttr2).floatValue()) != 0) {
                    return false;
                }
                break;
            }
            case LONG: {
                if (eventAttr1 != eventAttr2) {
                    return false;
                }
                break;
            }
            case STRING: {
                return eventAttr1.equals(eventAttr2);
            }
        }
        return true;
    }

    public static int getSize(EventComposite eventComposite) {
        int size = (DataBridgeCommonsUtils.getReferenceSize() * 3) + 4; // for agent size reference.
        size += eventComposite.getEventConverter().getSize(eventComposite.getEventBundle());
        return size;
    }

}
