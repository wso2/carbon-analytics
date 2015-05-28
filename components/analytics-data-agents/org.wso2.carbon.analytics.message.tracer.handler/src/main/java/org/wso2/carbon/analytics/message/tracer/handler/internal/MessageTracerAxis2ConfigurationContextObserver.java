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
package org.wso2.carbon.analytics.message.tracer.handler.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.handler.conf.EventingConfigData;
import org.wso2.carbon.analytics.message.tracer.handler.conf.RegistryPersistenceManager;
import org.wso2.carbon.analytics.message.tracer.handler.util.MessageTracerConstants;
import org.wso2.carbon.analytics.message.tracer.handler.util.TenantEventConfigData;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.Map;

/* This class extends AbstractAxis2ConfigurationContextObserver to engage Message trace module,
* when a new tenant is created.
*/
public class MessageTracerAxis2ConfigurationContextObserver extends
                                                            AbstractAxis2ConfigurationContextObserver {
    private static final Log LOG = LogFactory.getLog(MessageTracerAxis2ConfigurationContextObserver.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {

        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        setEventingConfigDataSpecificForTenant(tenantId);

        AxisModule messageTraceModule = axisConfiguration
                .getModule(MessageTracerConstants.ANALYTICS_SERVICE_MESSAGE_TRACER_MODULE_NAME);
        if (messageTraceModule != null) {
            try {
                axisConfiguration
                        .engageModule(MessageTracerConstants.ANALYTICS_SERVICE_MESSAGE_TRACER_MODULE_NAME);
            } catch (AxisFault e) {
                LOG.error("Cannot engage BAM MessageTracer module for the tenant :" + tenantId, e);
            }
        }
    }

    private void setEventingConfigDataSpecificForTenant(int tenantId) {
        Map<Integer, EventingConfigData> eventingConfigDataMap = TenantEventConfigData.getTenantSpecificEventingConfigData();
        RegistryPersistenceManager persistenceManager = new RegistryPersistenceManager();
        EventingConfigData eventingConfigData = persistenceManager.getEventingConfigData();
        eventingConfigDataMap.put(tenantId, eventingConfigData);
    }


    @Override
    public void terminatedConfigurationContext(ConfigurationContext configCtx) {
        // Do nothing
    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configCtx) {
        // Do nothing
    }
}