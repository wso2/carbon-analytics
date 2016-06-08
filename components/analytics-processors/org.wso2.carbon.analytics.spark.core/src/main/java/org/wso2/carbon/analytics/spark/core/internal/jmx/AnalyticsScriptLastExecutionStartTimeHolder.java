/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.internal.jmx;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 6/7/16.
 * <p/>
 * This class will keep last execution time of each scheduled analytics scripts.
 */
public class AnalyticsScriptLastExecutionStartTimeHolder {

    private static Map<String, Long> lastExecutionStartTime = new HashMap<>();

    private AnalyticsScriptLastExecutionStartTimeHolder() {
    }

    public static void add(String id, long timestamp) {
        lastExecutionStartTime.put(id, timestamp);
    }

    public static long get(String id) {
        Long timestamp = lastExecutionStartTime.get(id);
        if (timestamp != null) {
            return timestamp;
        }
        return -1;
    }

    public static String generateId(int tenantId, String scriptName) {
        return tenantId + "_" + scriptName;
    }
}
