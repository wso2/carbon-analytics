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

package org.wso2.carbon.data.provider;

import com.google.gson.JsonElement;
import org.wso2.carbon.data.provider.bean.DataSetMetadata;
import org.wso2.carbon.data.provider.exception.DataProviderException;

/**
 * Data provider interface.
 */
public interface DataProvider {

    /**
     * Initialize the instance with the session id.
     *
     * @param topic       to receive topic messages, data provider send event with this topic.
     * @param sessionId   data provider send event for this session.
     * @param jsonElement provider configuration message in json format.
     * @return data provider initialized with provider configuration.
     * @throws DataProviderException if there any initialization failures.
     */
    DataProvider init(String topic, String sessionId, JsonElement jsonElement) throws DataProviderException;

    /**
     * Start pushing data to the client from the data sources.
     */
    void start();

    /**
     * Stop pushing data to the client and disconnect.
     */
    void stop();

    /**
     * Provider configuration validator.
     *
     * @param providerConfig provider configuration get from the client.
     * @return validation results.
     * @throws DataProviderException if the validation failed due to exception.
     */
    boolean configValidator(ProviderConfig providerConfig) throws DataProviderException;

    /**
     * Get the data provider name.
     *
     * @return name of the data provider.
     */
    String providerName();

    /**
     * Get the data provider meta data object.
     *
     * @return meta data of the data provider.
     */
    DataSetMetadata dataSetMetadata();

    /**
     * Get the data provider configuration.
     *
     * @return configuration of the data provider.
     */
    String providerConfig();
}
