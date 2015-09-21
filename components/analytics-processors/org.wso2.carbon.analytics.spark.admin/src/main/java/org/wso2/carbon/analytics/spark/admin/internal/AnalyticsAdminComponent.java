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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;

/**
 * @scr.component name="analytics.admin" immediate="true"
 * @scr.reference name="analytics.core" interface="org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsProcessorService" unbind="unsetAnalyticsProcessorService"
 * @scr.reference name="hazelcast.instance.service" interface="com.hazelcast.core.HazelcastInstance"
 * cardinality="0..1" policy="dynamic" bind="setHazelcastInstance" unbind="unsetHazelcastInstance"
 */
public class AnalyticsAdminComponent {

    protected void activate(ComponentContext ctx) {
    }

    protected void setAnalyticsProcessorService(AnalyticsProcessorService analyticsService) {
        ServiceHolder.setAnalyticsProcessorService(analyticsService);
    }

    protected void unsetAnalyticsProcessorService(AnalyticsProcessorService analyticsService) {
        ServiceHolder.setAnalyticsProcessorService(null);
    }

    protected void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        ServiceHolder.setHazelcastInstance(hazelcastInstance);
    }

    protected void unsetHazelcastInstance(HazelcastInstance hazelcastInstance) {
        ServiceHolder.setHazelcastInstance(null);
    }
}
