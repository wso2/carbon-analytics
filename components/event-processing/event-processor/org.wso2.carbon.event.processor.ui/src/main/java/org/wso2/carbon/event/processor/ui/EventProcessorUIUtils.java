/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.processor.stub.EventProcessorAdminServiceStub;
import org.wso2.carbon.event.processor.stub.types.StreamDefinitionDto;
import org.wso2.carbon.event.stream.manager.stub.EventStreamAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class EventProcessorUIUtils {

    public static EventProcessorAdminServiceStub getEventProcessorAdminService(
            ServletConfig config, HttpSession session,
            HttpServletRequest request)
            throws AxisFault {
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        //Server URL which is defined in the server.xml
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session) + "EventProcessorAdminService.EventProcessorAdminServiceHttpsSoap12Endpoint";
        EventProcessorAdminServiceStub stub = new EventProcessorAdminServiceStub(configContext, serverURL);

        String cookie = (String) session.getAttribute(org.wso2.carbon.utils.ServerConstants.ADMIN_SERVICE_COOKIE);

        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub;
    }

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

    public static String getJsonStreamDefinition(StreamDefinitionDto streamDefinitionDto) {
//        JsonElement jsonElement = new JsonParser().parse(jsonStreamDef);
//        JsonObject rootObject = jsonElement.getAsJsonObject();
        JsonObject rootObject = new JsonObject();
        if (streamDefinitionDto == null) {
            return "";
        }
        rootObject.add("name", new JsonPrimitive(streamDefinitionDto.getName()));
        rootObject.add("version", new JsonPrimitive("1.0.0"));

        JsonArray metaData = new JsonArray();
        if (streamDefinitionDto.getMetaData() != null) {
            for (String metaAttribute : streamDefinitionDto.getMetaData()) {
                String[] attributeParts = metaAttribute.trim().split(" ");
                JsonObject attribute = new JsonObject();
                attribute.add("name", new JsonPrimitive(attributeParts[0]));
                attribute.add("type", new JsonPrimitive(attributeParts[1].toLowerCase()));
                metaData.add(attribute);
            }
        }
        rootObject.add("metaData", metaData);

        JsonArray correlationData = new JsonArray();
        if (streamDefinitionDto.getCorrelationData() != null) {
            for (String correlationAttribute : streamDefinitionDto.getCorrelationData()) {
                String[] attributeParts = correlationAttribute.trim().split(" ");
                JsonObject attribute = new JsonObject();
                attribute.add("name", new JsonPrimitive(attributeParts[0]));
                attribute.add("type", new JsonPrimitive(attributeParts[1].toLowerCase()));
                correlationData.add(attribute);
            }
        }
        rootObject.add("correlationData", correlationData);

        JsonArray payloadData = new JsonArray();
        if (streamDefinitionDto.getPayloadData() != null) {
            for (String payloadAttribute : streamDefinitionDto.getPayloadData()) {
                String[] attributeParts = payloadAttribute.trim().split(" ");
                JsonObject attribute = new JsonObject();
                attribute.add("name", new JsonPrimitive(attributeParts[0]));
                attribute.add("type", new JsonPrimitive(attributeParts[1].toLowerCase()));
                payloadData.add(attribute);
            }
        }
        rootObject.add("payloadData", payloadData);




        return rootObject.toString();
    }

}
