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
package org.wso2.carbon.analytics.eventsink.internal;

import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;

/**
 * This is the configuration class which specifies about actual queue length and bundle size of the events
 * that needs to be pushed immediatly to the data store.
 */
public class AnalyticsEventSinkConfiguration {
    private int queueSize;
    private int bundleSize;
    private static AnalyticsEventSinkConfiguration instance = new AnalyticsEventSinkConfiguration();

    private AnalyticsEventSinkConfiguration() {
        queueSize = AnalyticsEventSinkConstants.DEFAULT_EVENT_QUEUE_SIZE;
        bundleSize = AnalyticsEventSinkConstants.DEFAULT_BUNDLE_SIZE;
    }

    public static AnalyticsEventSinkConfiguration getInstance() {
        return instance;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getBundleSize() {
        return bundleSize;
    }

    public void setBundleSize(int bundleSize) {
        this.bundleSize = bundleSize;
    }
}
