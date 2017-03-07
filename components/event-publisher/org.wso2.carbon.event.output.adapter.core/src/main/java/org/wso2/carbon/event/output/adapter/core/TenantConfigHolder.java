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

import java.util.concurrent.ConcurrentHashMap;

public class TenantConfigHolder {
    private static ConcurrentHashMap<Integer, ConfigurationContext> tenantConfigs = new ConcurrentHashMap<>();

    public static void addTenantConfig(int tenantId, ConfigurationContext configurationContext){
        tenantConfigs.putIfAbsent(tenantId, configurationContext);
    }

    public static ConfigurationContext getTenantConfig(int tenantId){
        return tenantConfigs.get(tenantId);
    }

}
