/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bam.webapp.stat.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.Property;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.ServiceEventingConfigData;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

public class StreamDefinitionCreatorUtil {

    private static Log log = LogFactory.getLog(StreamDefinitionCreatorUtil.class);

    public static StreamDefinition getStreamDefinition(ServiceEventingConfigData configData) {
        StreamDefinition streamDefForServiceStats;

        StreamDefinition streamDef = null;
        streamDefForServiceStats = streamDefinitionForServiceStats(configData);
        streamDef = streamDefForServiceStats;

        return streamDef;
    }

    private static StreamDefinition streamDefinitionForServiceStats(ServiceEventingConfigData configData) {
        StreamDefinition streamDef = null;
        try {
            streamDef = new StreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            List<Attribute> metaDataAttributeList = new ArrayList<Attribute>();
            metaDataAttributeList = setUserAgentMetadata(metaDataAttributeList);
            metaDataAttributeList = setPropertiesAsMetaData(metaDataAttributeList, configData);

            streamDef.setMetaData(metaDataAttributeList);

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDef;
    }

    private static List<Attribute>  setPropertiesAsMetaData(List<Attribute> metaDataAttributeList,
                                                ServiceEventingConfigData configData) {
        Property[] properties = configData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaDataAttributeList.add(new Attribute(property.getKey(), AttributeType.STRING));
                }
            }
        }
        return metaDataAttributeList;
    }

    public static List<Attribute> addCommonPayLoadData(List<Attribute> payLoadData) {

        payLoadData.add(new Attribute("webappName" , AttributeType.STRING));
        payLoadData.add(new Attribute("webappVersion" , AttributeType.STRING));
        payLoadData.add(new Attribute("userId" , AttributeType.STRING));
        payLoadData.add(new Attribute("resourcePath" , AttributeType.STRING));
        payLoadData.add(new Attribute("webappType" , AttributeType.STRING));
        payLoadData.add(new Attribute("webappDisplayName" , AttributeType.STRING));
        payLoadData.add(new Attribute("webappContext" , AttributeType.STRING));
        payLoadData.add(new Attribute("sessionId" , AttributeType.STRING));
        payLoadData.add(new Attribute("httpMethod" , AttributeType.STRING));
        payLoadData.add(new Attribute("contentType" , AttributeType.STRING));
        payLoadData.add(new Attribute("responseContentType" , AttributeType.STRING));
        payLoadData.add(new Attribute("remoteAddress" , AttributeType.STRING));
        payLoadData.add(new Attribute("referer" , AttributeType.STRING));
        payLoadData.add(new Attribute("remoteUser" , AttributeType.STRING));
        payLoadData.add(new Attribute("authType" , AttributeType.STRING));
        payLoadData.add(new Attribute("userAgent" , AttributeType.STRING));
        payLoadData.add(new Attribute("browser" , AttributeType.STRING));
        payLoadData.add(new Attribute("browserVersion" , AttributeType.STRING));
        payLoadData.add(new Attribute("operatingSystem" , AttributeType.STRING));
        payLoadData.add(new Attribute("operatingSystemVersion" , AttributeType.STRING));
        payLoadData.add(new Attribute("searchEngine" , AttributeType.STRING));
        payLoadData.add(new Attribute("country" , AttributeType.STRING));
        payLoadData.add(new Attribute("timestamp" , AttributeType.LONG));
        payLoadData.add(new Attribute("responseHttpStatusCode" , AttributeType.INT));
        payLoadData.add(new Attribute("responseTime" , AttributeType.LONG));
        payLoadData.add(new Attribute("requestCount" , AttributeType.INT));
        payLoadData.add(new Attribute("responceCount" , AttributeType.INT));
        payLoadData.add(new Attribute("faultCount" , AttributeType.INT));
        payLoadData.add(new Attribute("requestSizeBytes" , AttributeType.LONG));
        payLoadData.add(new Attribute("responseSizeBytes" , AttributeType.LONG));

        return payLoadData;
    }



    public static List<Attribute> setUserAgentMetadata(List<Attribute> attributeList) {
        attributeList.add(new Attribute("serverAddess",
                AttributeType.STRING));
        attributeList.add(new Attribute("serverName",
                AttributeType.STRING));
        attributeList.add(new Attribute("tenantId",
                AttributeType.INT));
        attributeList.add(new Attribute("webappOwnerTenant",
                AttributeType.STRING));
        attributeList.add(new Attribute("userTenant",
                AttributeType.STRING));

        return attributeList;
    }
}
