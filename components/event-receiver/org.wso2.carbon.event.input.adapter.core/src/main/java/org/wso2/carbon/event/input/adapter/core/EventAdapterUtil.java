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

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.event.input.adapter.core.internal.ds.InputEventAdapterServiceValueHolder;
import org.wso2.carbon.event.stream.core.tenantconfigs.*;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class EventAdapterUtil {

    public static AxisConfiguration getAxisConfiguration() {
        AxisConfiguration axisConfiguration = null;
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfiguration = InputEventAdapterServiceValueHolder.getConfigurationContextService().
                    getServerConfigContext().getAxisConfiguration();
        } else {
            TenantConfigurationInfo tenantConfigurationInfo = TenantConfigHolder.getThreadLocalTenantConfiguration();
            if(tenantConfigurationInfo != null){
                if(tenantConfigurationInfo.getTenantId() == PrivilegedCarbonContext.getThreadLocalCarbonContext().
                        getTenantId()){
                    axisConfiguration = tenantConfigurationInfo.getConfigurationContext().getAxisConfiguration();
                }
            }else{
                axisConfiguration = TenantAxisUtils.getTenantAxisConfiguration(
                        CarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                        InputEventAdapterServiceValueHolder.getConfigurationContextService().getServerConfigContext());
            }
        }
        return axisConfiguration;
    }
}
