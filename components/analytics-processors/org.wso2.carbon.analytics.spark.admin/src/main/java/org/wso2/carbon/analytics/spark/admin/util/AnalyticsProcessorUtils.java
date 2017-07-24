/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.analytics.spark.admin.util;

import org.wso2.carbon.analytics.spark.admin.internal.ServiceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnalyticsProcessorUtils {
    private static final String RUNNING_SCRIPTS_MAP = "__RUNNING_SCRIPTS_MAP__";
    private static volatile ExecutorService executor = null;
    private static volatile Map<String, String> sessionIds;

    public static ExecutorService getExecutorServiceInstance() {
        if (executor == null) {
            synchronized (AnalyticsProcessorUtils.class) {
                if (executor == null) {
                    executor = Executors.newCachedThreadPool();
                }
            }
        }
        return executor;
    }

    public static Map<String, String> getRunningScriptsMap() {
        if (ServiceHolder.getHazelcastInstance() == null) {
            if (sessionIds == null) {
                synchronized (AnalyticsProcessorUtils.class) {
                    sessionIds = new HashMap<>();
                }
            }
            return sessionIds;
        } else {
            return ServiceHolder.getHazelcastInstance().getMap(RUNNING_SCRIPTS_MAP);
        }
    }
}