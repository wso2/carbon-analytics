/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.formatter.test.util;

import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.transport.adaptor.core.TransportAdaptorService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * singleton class to hold the transport adaptor service
 */
public final class TestEventFormatterServiceHolder {
    private static TestEventFormatterServiceHolder instance = new TestEventFormatterServiceHolder();
    private EventFormatterService eventFormatterService;
    private TransportAdaptorService transportAdaptorService;
    private ConfigurationContextService configurationContextService;

    private TestEventFormatterServiceHolder() {
    }

    public static TestEventFormatterServiceHolder getInstance() {
        return instance;
    }

    public TransportAdaptorService getTransportAdaptorService() {
        return transportAdaptorService;
    }

    public EventFormatterService getEventFormatterService() {
        return eventFormatterService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void registerEventFormatterService(EventFormatterService eventFormatterService) {
        this.eventFormatterService = eventFormatterService;
    }

    public void unregisterEventFormatterService(EventFormatterService eventFormatterService) {
        this.eventFormatterService = null;
    }

    public void registerTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        this.transportAdaptorService = transportAdaptorService;
    }

    public void unregisterTransportAdaptorService(TransportAdaptorService transportAdaptorService) {
        this.transportAdaptorService = null;
    }

    public void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public void unregisterConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = null;
    }
}
