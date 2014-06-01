/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.bam.message.tracer.handler.util;


import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherUtil;
import org.wso2.carbon.bam.message.tracer.handler.data.TracingInfo;
import org.wso2.carbon.bam.message.tracer.handler.internal.MessageTracerServiceComponent;
import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.http.HttpServletRequest;

public class AgentUtil {

    private static final String NAME = "Name";
    private static String serverName = null;

    public static void extractInfoFromHttpHeaders(TracingInfo eventData, Object requestProperty) {

        if (requestProperty instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) requestProperty;
            eventData.setRequestUrl(httpServletRequest.getRequestURL().toString());
            eventData.setRemoteAddress(PublisherUtil.getHostAddress());
            eventData.setContentType(httpServletRequest.getContentType());
            eventData.setUserAgent(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_USER_AGENT));
            eventData.setReferer(httpServletRequest.getHeader(
                    BAMDataPublisherConstants.HTTP_HEADER_REFERER));
        }
    }

    public static String getServerName() {
        if (serverName == null) {
            String[] properties = ServerConfiguration.getInstance().getProperties(NAME);
            if (properties != null && properties.length > 0) {
                serverName = properties[0];
            }
        }

        return serverName;
    }

    public static boolean allowedServices(String serverName) {
        return MessageTracerConstants.SYNAPSE_SERVICE.equals(serverName) ||
                MessageTracerConstants.MULTITENANT_SYNAPSE_SERVICE.equals(serverName) ||
                MessageTracerServiceComponent.getMessageTracerConfiguration().
                        getMessageTracingEnabledAdminServices().contains(serverName);
    }
}
