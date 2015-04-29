/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal.ds;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.processor.manager.core.internal.CarbonEventManagementService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="eventProcessorManagementService.component" immediate="true"
 * @scr.reference name="hazelcast.instance.service"
 * interface="com.hazelcast.core.HazelcastInstance" cardinality="0..1"
 * policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 * @scr.reference name="configuration.contextService.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */

public class EventManagementServiceDS {
    private static final Log log = LogFactory.getLog(EventManagementServiceDS.class);

    protected void activate(ComponentContext context) {
        try {

            CarbonEventManagementService eventManagementService = new CarbonEventManagementService();
            EventManagementServiceValueHolder.setEventManagementService(eventManagementService);

            context.getBundleContext().registerService(EventManagementService.class.getName(), eventManagementService, null);

            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventProcessorManagementService");
            }

        } catch (Throwable e) {
            log.error("Could not create EventProcessorManagementService: " + e.getMessage(), e);
        }

    }

    protected void deactivate(ComponentContext context) {
        EventManagementServiceValueHolder.getEventManagementService().shutdown();
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventManagementServiceValueHolder.registerHazelcastInstance(hazelcastInstance);

        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {

            }

            @Override
            public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

            }

        });
        EventManagementServiceValueHolder.getEventManagementService().init(hazelcastInstance);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        EventManagementServiceValueHolder.registerHazelcastInstance(null);
    }

//    public void setEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
//        EventManagementServiceValueHolder.registerEventProcessorManagementService(eventProcessorManagementService);
//    }
//
//    public void unSetEventProcessorManagementService(EventProcessorManagementService eventProcessorManagementService) {
//        EventManagementServiceValueHolder.registerEventProcessorManagementService(null);
//    }

//    public void setEventReceiverManagementService(EventReceiverManagementService eventReceiverManagementService) {
//        EventManagementServiceValueHolder.registerEventReceiverManagementService(eventReceiverManagementService);
//    }
//
//    public void unSetEventReceiverManagementService(EventReceiverManagementService eventReceiverManagementService) {
//        EventManagementServiceValueHolder.registerEventReceiverManagementService(null);
//    }

//    public void setEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
//        EventManagementServiceValueHolder.registerEventPublisherManagementService(eventPublisherManagementService);
//    }
//
//    public void unSetEventPublisherManagementService(EventPublisherManagementService eventPublisherManagementService) {
//        EventManagementServiceValueHolder.registerEventPublisherManagementService(null);
//    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EventManagementServiceValueHolder.getEventManagementService().init(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {
    }
}
