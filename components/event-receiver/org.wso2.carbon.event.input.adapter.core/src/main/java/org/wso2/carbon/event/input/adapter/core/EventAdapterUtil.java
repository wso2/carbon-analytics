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

package org.wso2.carbon.event.input.adapter.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.internal.config.AdapterConfig;
import org.wso2.carbon.event.input.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.event.input.adapter.core.internal.ds.InputEventAdapterServiceValueHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Map;

public class EventAdapterUtil {

    public static AxisConfiguration getAxisConfiguration() {
        AxisConfiguration axisConfiguration = null;
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfiguration = InputEventAdapterServiceValueHolder.getConfigurationContextService().
                    getServerConfigContext().getAxisConfiguration();
        } else {
            ConfigurationContext configurationContext = TenantConfigHolder
                    .getTenantConfig(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            if (configurationContext != null) {
                axisConfiguration = configurationContext.getAxisConfiguration();
            } else {
                throw new InputEventAdapterRuntimeException("Tenant configuration not found");
            }
        }
        return axisConfiguration;
    }


    public static Map<String, String> getGlobalProperties(String type) {
        AdapterConfigs adapterConfigs = InputEventAdapterServiceValueHolder.getGlobalAdapterConfigs();
        if (adapterConfigs != null) {
            AdapterConfig adapterConfig = adapterConfigs.getAdapterConfig(type);
            if (adapterConfig != null) {
                return adapterConfig.getGlobalPropertiesAsMap();
            }
        }
        return null;
    }
}
