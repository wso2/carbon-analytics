/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.extensions.installer.core.models;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains name and body of Siddhi apps which are present in the runtime.
 * This is updated during deployments or un-deployments of Siddhi apps, performed in the runtime.
 */
public class SiddhiAppStore {

    private ConcurrentMap<String, String> siddhiApps = new ConcurrentHashMap<>();

    public void addOrUpdateSiddhiApp(String siddhiAppName, String siddhiAppBody) {
        siddhiApps.put(siddhiAppName, siddhiAppBody);
    }

    public ConcurrentMap<String, String> getSiddhiApps() {
        return siddhiApps;
    }

    public void removeSiddhiApp(String siddhiAppName) {
        siddhiApps.remove(siddhiAppName);
    }
}
