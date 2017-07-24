/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.spark.admin.internal;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;

/**
 * Class to hold the osgi service registration instances.
 */
public class ServiceHolder {

    private ServiceHolder() {
        /**
         * Avoid instantiation
         */
    }

    private static AnalyticsProcessorService analyticsProcessorService;
    private static HazelcastInstance hazelcastInstance;

    public static AnalyticsProcessorService getAnalyticsProcessorService() {
        return analyticsProcessorService;
    }

    public static void setAnalyticsProcessorService(AnalyticsProcessorService analyticsProcessorService) {
        ServiceHolder.analyticsProcessorService = analyticsProcessorService;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        ServiceHolder.hazelcastInstance = hazelcastInstance;
    }
}
