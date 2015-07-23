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

package org.wso2.carbon.event.stream.core.tenantconfigs;

import org.apache.axis2.context.ConfigurationContext;

public class TenantConfigurationInfo {

    private int tenantId;
    private ConfigurationContext configurationContext;

    public TenantConfigurationInfo(int tenantId, ConfigurationContext configurationContext){
        this.tenantId = tenantId;
        this.configurationContext = configurationContext;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
}
