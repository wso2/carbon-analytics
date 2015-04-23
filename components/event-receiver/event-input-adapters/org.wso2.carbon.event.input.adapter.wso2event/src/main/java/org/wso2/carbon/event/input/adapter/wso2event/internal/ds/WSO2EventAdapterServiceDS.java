/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adapter.wso2event.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.wso2event.WSO2EventAdapter;
import org.wso2.carbon.event.input.adapter.wso2event.WSO2EventEventAdapterFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @scr.component name="input.wso2EventAdapterService.component" immediate="true"
 * @scr.reference name="agentserverservice.service"
 * interface="org.wso2.carbon.databridge.core.DataBridgeSubscriberService" cardinality="1..1"
 * policy="dynamic" bind="setDataBridgeSubscriberService" unbind="unSetDataBridgeSubscriberService"
 */


public class WSO2EventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(WSO2EventAdapterServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */


    protected void activate(ComponentContext context) {

        try {
            InputEventAdapterFactory wso2EventEventAdapterFactory = new WSO2EventEventAdapterFactory();
            context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(), wso2EventEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input WSO2Event adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the input WSO2Event adapter service ", e);
        }
    }

    protected void setDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {
        if (WSO2EventAdapterServiceValueHolder.getDataBridgeSubscriberService() == null) {
            WSO2EventAdapterServiceValueHolder.registerDataBridgeSubscriberService(dataBridgeSubscriberService);

            dataBridgeSubscriberService.subscribe(new AgentCallback() {
                @Override
                public void definedStream(StreamDefinition streamDefinition, int i) {

                }

                @Override
                public void removeStream(StreamDefinition streamDefinition, int i) {

                }

                @Override
                public void receive(List<Event> events, Credentials credentials) {
                    try {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(credentials.getTenantId());
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(credentials.getDomainName());

                        for (Event event : events) {
                            ConcurrentHashMap<String, WSO2EventAdapter> adapters = WSO2EventAdapterServiceValueHolder.getAdapterService(credentials.getDomainName(), event.getStreamId());
                            if (adapters != null) {
                                for (WSO2EventAdapter adapter : adapters.values()) {
                                    adapter.getEventAdaptorListener().onEvent(event);
                                }
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Event received in wso2Event Adapter - " + event);
                            }
                        }
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }

                }
            });
        }
    }

    protected void unSetDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {

    }

}
