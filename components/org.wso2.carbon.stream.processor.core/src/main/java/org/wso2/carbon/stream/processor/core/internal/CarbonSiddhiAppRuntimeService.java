/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.stream.processor.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.siddhi.core.SiddhiAppRuntime;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the implementations of the apis related to SiddhiAppRuntimes
 */
public class CarbonSiddhiAppRuntimeService implements SiddhiAppRuntimeService {
    private static final Logger log = LoggerFactory.getLogger(CarbonSiddhiAppRuntimeService.class.getName());

    @Override
    public Map<String, SiddhiAppRuntime> getActiveSiddhiAppRuntimes() {
        Map<String, SiddhiAppData> siddhiApps =
                StreamProcessorDataHolder.getStreamProcessorService().getSiddhiAppMap();
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = new HashMap<>();
        for (Map.Entry<String, SiddhiAppData> entry : siddhiApps.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isActive()) {
                siddhiAppRuntimes.put(entry.getKey(), entry.getValue().getSiddhiAppRuntime());
            }
        }
        return siddhiAppRuntimes;
    }

    @Override
    public void enableSiddhiAppStatistics(boolean statsEnabled) {
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = getActiveSiddhiAppRuntimes();
        for (Map.Entry<String, SiddhiAppRuntime> siddhiRuntimeEntry: siddhiAppRuntimes.entrySet()) {
            if ((statsEnabled && !siddhiRuntimeEntry.getValue().isStatsEnabled()) || (!statsEnabled &&
                    siddhiRuntimeEntry.getValue().isStatsEnabled())) {
                siddhiRuntimeEntry.getValue().enableStats(statsEnabled);
                if (log.isDebugEnabled()) {
                    log.info("Stats has been sucessfull updated for siddhi app :" + siddhiRuntimeEntry.getKey());
                }
            }
        }
    }
}
