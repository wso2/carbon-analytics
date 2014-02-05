/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.event.input.adaptor.wso2event.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorFactory;
import org.wso2.carbon.event.input.adaptor.wso2event.WSO2EventEventAdaptorFactory;


/**
 * @scr.component name="input.wso2EventAdaptorService.component" immediate="true"
 * @scr.reference name="agentserverservice.service"
 * interface="org.wso2.carbon.databridge.core.DataBridgeSubscriberService" cardinality="1..1"
 * policy="dynamic" bind="setDataBridgeSubscriberService" unbind="unSetDataBridgeSubscriberService"
 * @scr.reference name="agentservice.service"
 * interface="org.wso2.carbon.databridge.agent.thrift.Agent" cardinality="1..1"
 * policy="dynamic" bind="setAgent" unbind="unSetAgent"
 */


public class WSO2EventAdaptorServiceDS {

    private static final Log log = LogFactory.getLog(WSO2EventAdaptorServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */


    protected void activate(ComponentContext context) {

        try {
            InputEventAdaptorFactory wso2EventEventAdaptorFactory = new WSO2EventEventAdaptorFactory();
            context.getBundleContext().registerService(InputEventAdaptorFactory.class.getName(), wso2EventEventAdaptorFactory, null);
            log.info("Successfully deployed the input WSO2Event adaptor service");
        } catch (RuntimeException e) {
            log.error("Can not create the input WSO2Event adaptor service ", e);
        }
    }

    protected void setDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {
        WSO2EventAdaptorServiceValueHolder.registerDataBridgeSubscriberService(dataBridgeSubscriberService);
    }

    protected void unSetDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {

    }

    protected void setAgent(Agent agent) {
        WSO2EventAdaptorServiceValueHolder.registerAgent(agent);
    }

    protected void unSetAgent(Agent agent) {

    }

}
