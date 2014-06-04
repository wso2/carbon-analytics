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
package org.wso2.carbon.bam.webapp.stat.publisher.internal;


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.RegistryPersistenceManager;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.ServiceEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.publish.StreamDefinitionCreatorUtil;
import org.wso2.carbon.bam.webapp.stat.publisher.util.TenantEventConfigData;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.Map;

/* This class extends AbstractAxis2ConfigurationContextObserver to engage Service stats module,
* when a new tenant is created.
*/
public class WebappStatisticsAxis2ConfigurationContextObserver extends
        AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(WebappStatisticsAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configContext) {

        //Enaging module for the tenant if the service publishing is enabled in the bam.xml
        if (WebappStatisticsServiceComponent.isPublishingEnabled()) {
            AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();

            setEventingConfigDataSpecificForTenant(axisConfiguration);
        }
    }

    private void setEventingConfigDataSpecificForTenant(AxisConfiguration axisConfiguration) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<Integer, InternalEventingConfigData> eventingConfigDataMap = TenantEventConfigData.getTenantSpecificEventingConfigData();
        RegistryPersistenceManager persistenceManager = new RegistryPersistenceManager();
        ServiceEventingConfigData eventingConfigData = persistenceManager.getEventingConfigData();
        InternalEventingConfigData eventConfigNStreamDef = new RegistryPersistenceManager().
                fillEventingConfigData(eventingConfigData);

        StreamDefinition streamDefinition = StreamDefinitionCreatorUtil.getStreamDefinition(eventingConfigData);
        eventConfigNStreamDef.setStreamDefinition(streamDefinition);
        eventingConfigDataMap.put(tenantID, eventConfigNStreamDef);
    }


    public void terminatedConfigurationContext(ConfigurationContext configCtx) {

    }

    public void terminatingConfigurationContext(ConfigurationContext configCtx) {

    }

}