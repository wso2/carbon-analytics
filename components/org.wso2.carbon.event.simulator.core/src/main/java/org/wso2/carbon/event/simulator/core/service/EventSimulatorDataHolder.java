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

package org.wso2.carbon.event.simulator.core.service;

import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.stream.processor.common.EventStreamService;

/**
 * EventSimulaorDataHolder referenced through ServiceComponent
 */
public class EventSimulatorDataHolder {
    private static EventSimulatorDataHolder instance = new EventSimulatorDataHolder();
    private long maximumFileSize;
    private String csvFileDirectory;
    private EventStreamService eventStreamService;
    private static PermissionProvider permissionProvider;


    private EventSimulatorDataHolder() {
    }

    public static EventSimulatorDataHolder getInstance() {
        return instance;
    }

    public long getMaximumFileSize() {
        return maximumFileSize;
    }

    public void setMaximumFileSize(long maximumFileSize) {
        this.maximumFileSize = maximumFileSize;
    }

    public EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public void setEventStreamService(EventStreamService eventStreamService) {
        this.eventStreamService = eventStreamService;
    }

    public String getCsvFileDirectory() {
        return csvFileDirectory;
    }

    public void setCsvFileDirectory(String csvFileDirectory) {
        this.csvFileDirectory = csvFileDirectory;
    }

    public static PermissionProvider getPermissionProvider() {
        return EventSimulatorDataHolder.permissionProvider;
    }

    public static void setPermissionProvider(PermissionProvider permissionProvider) {
        EventSimulatorDataHolder.permissionProvider = permissionProvider;
    }
}
