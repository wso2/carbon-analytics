/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.parser;

import io.siddhi.core.SiddhiManager;
import org.wso2.carbon.config.provider.ConfigProvider;

public class SiddhiParserDataHolder {
    private static ConfigProvider configFileReader;
    private static SiddhiManager siddhiManager;

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void setSiddhiManager(SiddhiManager siddhiManager) {
        SiddhiParserDataHolder.siddhiManager = siddhiManager;
    }

    public static ConfigProvider getConfigProvider() {
        return configFileReader;
    }

    public static void setConfigProvider(ConfigProvider configProvider) {
        SiddhiParserDataHolder.configFileReader = configProvider;
    }

}
