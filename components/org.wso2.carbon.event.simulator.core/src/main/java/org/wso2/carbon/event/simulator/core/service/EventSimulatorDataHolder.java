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

import org.wso2.carbon.stream.processor.common.EventStreamService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventSimulaorDataHolder referenced through ServiceComponent
 */
public class EventSimulatorDataHolder {
    private static EventSimulatorDataHolder instance = new EventSimulatorDataHolder();
    private String directoryDestination;
    private long maximumFileSize;
    private EventStreamService eventStreamService;


    private EventSimulatorDataHolder() {
    }

    /**
     * This returns the EventSimulatorDataHolder instance.
     *
     * @return The EventSimulatorDataHolder instance of this singleton class
     */

    public static EventSimulatorDataHolder getInstance() {
        return instance;
    }

    public String getDirectoryDestination() {
        return directoryDestination;
    }

    public void setDirectoryDestination(String directoryDestination) {
        this.directoryDestination = directoryDestination;
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

}
