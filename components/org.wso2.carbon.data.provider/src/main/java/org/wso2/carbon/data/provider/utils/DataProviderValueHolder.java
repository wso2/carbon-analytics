/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.data.provider.utils;

import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.DataProviderAuthorizer;
import org.wso2.carbon.datasource.core.api.DataSourceService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Value holder for data provider.
 */
public class DataProviderValueHolder {
    private static DataProviderValueHolder dataProviderHelper = new DataProviderValueHolder();
    private DataSourceService dataSourceService = null;
    private ConfigProvider configProvider = null;
    private Map<String, Map<String, DataProvider>> sessionDataProviderMap = new ConcurrentHashMap<>();
    private Map<String, Class> dataProviderClassMap = new ConcurrentHashMap<>();
    private Map<String, DataProviderAuthorizer> dataProviderAuthorizerClassMap = new ConcurrentHashMap<>();

    public static DataProviderValueHolder getDataProviderHelper() {
        return dataProviderHelper;
    }

    public DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public DataProviderAuthorizer getDataProviderAuthorizer(String dataProviderAuthorizerClassName) {
        return this.dataProviderAuthorizerClassMap.get(dataProviderAuthorizerClassName);
    }

    public void setDataProviderAuthorizer(String dataProviderAuthorizerClassName,
                                          DataProviderAuthorizer dataProviderAuthorizer) {
        this.dataProviderAuthorizerClassMap.put(dataProviderAuthorizerClassName, dataProviderAuthorizer);
    }

    public void setDataProvider(String providerName, DataProvider dataProvider) {
        this.dataProviderClassMap.put(providerName, dataProvider.getClass());
    }

    public DataProvider getDataProvider(String providerName) throws IllegalAccessException, InstantiationException {
        return (DataProvider) this.dataProviderClassMap.get(providerName).newInstance();
    }

    public Set<String> getDataProviderNameSet() {
        return dataProviderClassMap.keySet();
    }

    public Map<String, DataProvider> getTopicDataProviderMap(String sessionId) {
        return sessionDataProviderMap.get(sessionId);
    }

    public void removeDataProviderClass(String providerName) {
        this.dataProviderClassMap.remove(providerName);
    }

    public void removeDataProviderAuthorizerClass(String dataProviderAuthorizerClassName) {
        this.dataProviderAuthorizerClassMap.remove(dataProviderAuthorizerClassName);
    }

    public void removeSessionData(String sessionId) {
        this.sessionDataProviderMap.remove(sessionId);
    }

    public boolean removeTopicIfExist(String sessionId, String topic) {
        if (this.sessionDataProviderMap.containsKey(sessionId)) {
            if (this.sessionDataProviderMap.get(sessionId).containsKey(topic)) {
                DataProvider dataProvider = this.sessionDataProviderMap.get(sessionId).remove(topic);
                if (dataProvider != null) {
                    dataProvider.stop();
                }
                return true;
            }
        }
        return false;
    }

    public void addDataProviderToSessionMap(String sessionId, String topic, DataProvider dataProvider) {
        if (this.sessionDataProviderMap.containsKey(sessionId)) {
            this.sessionDataProviderMap.get(sessionId).put(topic, dataProvider);
        } else {
            this.sessionDataProviderMap.put(sessionId, new ConcurrentHashMap<>());
            this.sessionDataProviderMap.get(sessionId).put(topic, dataProvider);
        }
    }
}
