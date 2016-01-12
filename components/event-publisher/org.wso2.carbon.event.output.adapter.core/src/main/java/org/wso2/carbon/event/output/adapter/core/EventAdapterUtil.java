/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.output.adapter.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfig;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Map;

public class EventAdapterUtil {

    public static AxisConfiguration getAxisConfiguration() {
        AxisConfiguration axisConfiguration = null;
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfiguration = OutputEventAdapterServiceValueHolder.getConfigurationContextService().
                    getServerConfigContext().getAxisConfiguration();
        } else {
            ConfigurationContext configurationContext = TenantConfigHolder
                    .getTenantConfig(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            if (configurationContext != null) {
                axisConfiguration = configurationContext.getAxisConfiguration();
            } else {
                throw new OutputEventAdapterRuntimeException("Tenant configuration not found");
            }
        }
        return axisConfiguration;
    }

    public static void logAndDrop(String adapterName, Object event, String message, Throwable e, Log log,
                                  int tenantId) {
        if (message != null) {
            message = message + ", ";
        } else {
            message = "";
        }
        log.error("Event dropped at Output Adapter '" + adapterName + "' for tenant id '" + tenantId + "', " + message + e
                .getMessage(), e);
        if (log.isDebugEnabled()) {
            log.debug("Error at Output Adapter '" + adapterName + "' for tenant id '" + tenantId + "', dropping event: \n"
                    + event, e);
        }
    }

    public static void logAndDrop(String adapterName, Object event, String message, Log log, int tenantId) {
        log.error("Event dropped at Output Adapter '" + adapterName + "' for tenant id '" + tenantId + "', " + message);
        if (log.isDebugEnabled()) {
            log.debug("Error at Output Adapter '" + adapterName + "' for tenant id '" + tenantId + "', dropping event: \n"
                    + event);
        }
    }

    public static Map<String, String> getGlobalProperties(String type) {
        AdapterConfigs adapterConfigs = OutputEventAdapterServiceValueHolder.getGlobalAdapterConfigs();
        if (adapterConfigs != null) {
            AdapterConfig adapterConfig = adapterConfigs.getAdapterConfig(type);
            if (adapterConfig != null) {
                return adapterConfig.getGlobalPropertiesAsMap();
            }
        }
        return null;
    }

}
