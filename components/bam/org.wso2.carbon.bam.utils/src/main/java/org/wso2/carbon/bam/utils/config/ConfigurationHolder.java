/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.utils.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationHolder {

    // TODO : Make this a write through cache for meta column family
    private Map<Integer, List<CFConfigBean>> indexConfigurations;

    private static ConfigurationHolder instance = new ConfigurationHolder();

    // The tenant whose config is currently being processed. This is used at config loading time to
    // keep track which tenant config is being loaded. Used in correctly storing column family
    // configuration against tenants during the initialization of Indexing analyzer. See IndexingAnalyzerBuilder.
    private int currentTenant;

    private ConfigurationHolder() {
        this.indexConfigurations = new ConcurrentHashMap<Integer, List<CFConfigBean>>();
    }

    public static ConfigurationHolder getInstance() {
        return instance;
    }

    public List<CFConfigBean> getIndexConfigurations(int tenantId) {
        return indexConfigurations.get(tenantId);
    }

    public CFConfigBean getIndexConfiguration(String cfName, int tenantId) {
        List<CFConfigBean> cfConfigList = indexConfigurations.get(tenantId);

        if (cfConfigList != null) {
            for (CFConfigBean cfConfig : cfConfigList) {
                if (cfConfig.getCfName().equals(cfName)) {
                    return cfConfig;
                }
            }
        }

        return null;
    }

    public void addIndexConfigurations(int tenantId, List<CFConfigBean> indexConfigs) {
        List<CFConfigBean> list = indexConfigurations.get(tenantId);

        if (list == null) {
            indexConfigurations.put(tenantId, list);
            return;
        } else {
            list.addAll(indexConfigs);
        }
    }

    public void addIndexConfiguration(int tenantId, CFConfigBean indexConfiguration) {
        List<CFConfigBean> list = indexConfigurations.get(tenantId);

        if (list == null) {
            list = Collections.synchronizedList(new ArrayList<CFConfigBean>());
            indexConfigurations.put(tenantId, list);
        }
        if (!list.contains(indexConfiguration)) {
            list.add(indexConfiguration);
        }
    }

    public void resetIndexConfigurations(int tenantId, List<CFConfigBean> indexConfigs) {
        indexConfigurations.put(tenantId, Collections.synchronizedList(indexConfigs));
    }

    public void deleteIndexConfigurations(int tenantId) {
        indexConfigurations.put(tenantId, null);
    }

    public void setCurrentConfigProcessingTenant(int tenantId) {
        this.currentTenant = tenantId;
    }

    public int getCurrentConfigProcessingTenant() {
        return this.currentTenant;
    }

}
