/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.core.internal.ds;

import org.wso2.carbon.event.input.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.event.statistics.EventStatisticsService;
import org.wso2.carbon.event.input.adapter.core.internal.CarbonInputEventAdapterService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class InputEventAdapterServiceValueHolder {

    private static CarbonInputEventAdapterService carbonInputEventAdapterService;
    private static RegistryService registryService;
    private static EventStatisticsService eventStatisticsService;
    private static AdapterConfigs globalAdapterConfigs;
    private static ConfigurationContextService configurationContextService;


    private InputEventAdapterServiceValueHolder() {

    }

    public static CarbonInputEventAdapterService getCarbonInputEventAdapterService() {
        return carbonInputEventAdapterService;
    }

    public static void setCarbonInputEventAdapterService(CarbonInputEventAdapterService carbonInputEventAdapterService) {
        InputEventAdapterServiceValueHolder.carbonInputEventAdapterService = carbonInputEventAdapterService;
    }

    public static RegistryService getRegistryService() {
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        InputEventAdapterServiceValueHolder.registryService = registryService;
    }

    public static EventStatisticsService getEventStatisticsService() {
        return eventStatisticsService;
    }

    public static void setEventStatisticsService(EventStatisticsService eventStatisticsService) {
        InputEventAdapterServiceValueHolder.eventStatisticsService = eventStatisticsService;
    }

    public static void setGlobalAdapterConfigs(AdapterConfigs globalAdapterConfigs) {
        InputEventAdapterServiceValueHolder.globalAdapterConfigs = globalAdapterConfigs;
    }

    public static AdapterConfigs getGlobalAdapterConfigs() {
        return globalAdapterConfigs;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        InputEventAdapterServiceValueHolder.configurationContextService = configurationContextService;
    }
}
