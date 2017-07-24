/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.message.tracer.internal.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.data.EventStreamDef;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

public class StreamDefUtil {

    protected static final String CONTENT = "content";
    protected static final String TYPE = "type";
    protected static final String REQUEST_METHOD = "request_method";
    protected static final String RESOURCE_URL = "resource_url";
    protected static final String TIMESTAMP = "timestamp";
    protected static final String HOST = "host";
    protected static final String ACTIVITY_ID = "activity_id";
    public static final String STATUS = "status";

    private static Log log = LogFactory.getLog(StreamDefUtil.class);

    public static StreamDefinition getStreamDefinition(EventStreamDef eventStreamDef) {

        StreamDefinition streamDefinition = null;
        try {
            streamDefinition = new StreamDefinition(eventStreamDef.getStreamName(),
                                                    eventStreamDef.getVersion());
            streamDefinition.setNickName(eventStreamDef.getNickName());
            streamDefinition.setDescription(eventStreamDef.getDescription());

            List<Attribute> metaDataAttributeList = getMetaDataDef();
            streamDefinition.setMetaData(metaDataAttributeList);

            List<Attribute> correlationDataAttributeList = getCorrelationDataDef();
            streamDefinition.setCorrelationData(correlationDataAttributeList);

            List<Attribute> payLoadData = getPayLoadDataDef();
            streamDefinition.setPayloadData(payLoadData);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to create StreamDefinition : " + e.getErrorMessage(), e);
        }

        return streamDefinition;
    }

    private static List<Attribute> getPayLoadDataDef() {

        List<Attribute> payLoadList = new ArrayList<Attribute>(6);
        payLoadList.add(new Attribute(CONTENT, AttributeType.STRING));
        payLoadList.add(new Attribute(TYPE, AttributeType.STRING));
        payLoadList.add(new Attribute(REQUEST_METHOD, AttributeType.STRING));
        payLoadList.add(new Attribute(RESOURCE_URL, AttributeType.STRING));
        payLoadList.add(new Attribute(TIMESTAMP, AttributeType.LONG));
        payLoadList.add(new Attribute(STATUS, AttributeType.STRING));

        return payLoadList;
    }

    private static List<Attribute> getMetaDataDef() {

        List<Attribute> metaDataList = new ArrayList<Attribute>(1);
        metaDataList.add(new Attribute(HOST, AttributeType.STRING));
        return metaDataList;
    }

    private static List<Attribute> getCorrelationDataDef() {
        List<Attribute> correlationList = new ArrayList<Attribute>(1);
        correlationList.add(new Attribute(ACTIVITY_ID, AttributeType.STRING));
        return correlationList;
    }
}
