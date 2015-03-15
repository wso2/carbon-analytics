/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.stream.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto;
import org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class EventStreamUIUtils {

    public static EventStreamAdminServiceStub getEventStreamAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                                                     session) + "EventStreamAdminService.EventStreamAdminServiceHttpsSoap12Endpoint";
        EventStreamAdminServiceStub stub = new EventStreamAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

    public static EventStreamDefinitionDto getEventStreamInfoDtoFrom(String jsonStreamDef) {
        JsonElement jsonElement = new JsonParser().parse(jsonStreamDef);
        JsonObject rootObject = jsonElement.getAsJsonObject();
        String streamName = rootObject.getAsJsonPrimitive("name").getAsString();
        String streamVersion = rootObject.getAsJsonPrimitive("version").getAsString();
        EventStreamDefinitionDto eventStreamInfoDto = new EventStreamDefinitionDto();
        eventStreamInfoDto.setName(streamName);
        eventStreamInfoDto.setVersion(streamVersion);
        JsonArray metaData = rootObject.getAsJsonArray("metaData");
        JsonArray correlationData = rootObject.getAsJsonArray("correlationData");
        JsonArray payloadData = rootObject.getAsJsonArray("payloadData");
        List<EventStreamAttributeDto> metaAttributeDtos = new ArrayList<EventStreamAttributeDto>();
        for (JsonElement metaAttribute : metaData) {
            JsonObject metaAttributeObj = metaAttribute.getAsJsonObject();
            EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
            eventStreamAttributeDto.setAttributeName(metaAttributeObj.getAsJsonPrimitive("name").getAsString());
            eventStreamAttributeDto.setAttributeType(metaAttributeObj.getAsJsonPrimitive("type").getAsString());
            metaAttributeDtos.add(eventStreamAttributeDto);
        }
        List<EventStreamAttributeDto> correlationAttributeDtos = new ArrayList<EventStreamAttributeDto>();
        for (JsonElement correlationAttribute : correlationData) {
            JsonObject metaAttributeObj = correlationAttribute.getAsJsonObject();
            EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
            eventStreamAttributeDto.setAttributeName(metaAttributeObj.getAsJsonPrimitive("name").getAsString());
            eventStreamAttributeDto.setAttributeType(metaAttributeObj.getAsJsonPrimitive("type").getAsString());
            correlationAttributeDtos.add(eventStreamAttributeDto);
        }
        List<EventStreamAttributeDto> payloadAttributeDtos = new ArrayList<EventStreamAttributeDto>();
        for (JsonElement payloadAttribute : payloadData) {
            JsonObject metaAttributeObj = payloadAttribute.getAsJsonObject();
            EventStreamAttributeDto eventStreamAttributeDto = new EventStreamAttributeDto();
            eventStreamAttributeDto.setAttributeName(metaAttributeObj.getAsJsonPrimitive("name").getAsString());
            eventStreamAttributeDto.setAttributeType(metaAttributeObj.getAsJsonPrimitive("type").getAsString());
            payloadAttributeDtos.add(eventStreamAttributeDto);
        }
        if (metaAttributeDtos.size() > 0) {
            eventStreamInfoDto.setMetaData(metaAttributeDtos.toArray(new EventStreamAttributeDto[metaAttributeDtos.size()]));
        }
        if (correlationAttributeDtos.size() > 0) {
            eventStreamInfoDto.setCorrelationData(correlationAttributeDtos.toArray(new EventStreamAttributeDto[correlationAttributeDtos.size()]));
        }
        if (payloadAttributeDtos.size() > 0) {
            eventStreamInfoDto.setPayloadData(payloadAttributeDtos.toArray(new EventStreamAttributeDto[payloadAttributeDtos.size()]));
        }

        return eventStreamInfoDto;
    }


}
