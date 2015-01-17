/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasink.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.datasink.AnalyticsDSConnector;
import org.wso2.carbon.analytics.datasink.internal.util.ServiceHolder;
import org.wso2.carbon.analytics.datasink.subscriber.AnalyticsEventStreamListener;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * @scr.component name="analytics.datasink.comp" immediate="true"
 * @scr.reference name="registry.streamdefn.comp"
 * interface="org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore"
 * cardinality="1..1" policy="dynamic" bind="setStreamDefinitionStoreService" unbind="unsetStreamDefinitionStoreService"
 * @scr.reference name="event.stream.service" interface="org.wso2.carbon.event.stream.manager.core.EventStreamService"
 * cardinality="1..1" policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="analytics.component" interface="org.wso2.carbon.analytics.dataservice.AnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataservice" unbind="unsetAnalyticsDataservice"
 */

public class AnalyticsDatasinkComponent {
    private static Log log = LogFactory.getLog(AnalyticsDatasinkComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Started the Data bridge Analytics Data Sink component");
        }
        componentContext.getBundleContext().registerService(Axis2ConfigurationContextObserver.class.getName(),
                new AnalyticsDatasinkConfigurationContextObserver(), null);

        ServiceHolder.getAnalyticsEventStreamListener().loadEventStreams(MultitenantConstants.SUPER_TENANT_ID);
        ServiceHolder.setAnalyticsDSConnector(new AnalyticsDSConnector());
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the Data bridge Cassandra Data Sink component");
        }
    }

    protected void setStreamDefinitionStoreService(AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        ServiceHolder.setStreamDefinitionStoreService(abstractStreamDefinitionStore);
    }

    protected void unsetStreamDefinitionStoreService(AbstractStreamDefinitionStore abstractStreamDefinitionStore) {
        ServiceHolder.setStreamDefinitionStoreService(null);
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setAnalyticsEventStreamListener(new AnalyticsEventStreamListener());

        eventStreamService.registerEventStreamListener(ServiceHolder.getAnalyticsEventStreamListener());
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(null);
    }

    protected void setAnalyticsDataservice(AnalyticsDataService analyticsDataservice) {
        ServiceHolder.setAnalyticsDataService(analyticsDataservice);
    }

    protected void unsetAnalyticsDataservice(AnalyticsDataService analyticsDataservice) {
        ServiceHolder.setAnalyticsDataService(null);
    }

}
