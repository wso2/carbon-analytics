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
package org.wso2.carbon.analytics.datasink.internal;

import org.wso2.carbon.analytics.datasink.internal.util.AnalyticsDatasinkConstants;

public class AnalyticsDatasinkConfiguration {
    private int queueSize;
    private int bundleSize;
    private static AnalyticsDatasinkConfiguration instance = new AnalyticsDatasinkConfiguration();

    private AnalyticsDatasinkConfiguration() {
        queueSize = AnalyticsDatasinkConstants.DEFAULT_EVENT_QUEUE_SIZE;
        bundleSize = AnalyticsDatasinkConstants.DEFAULT_BUNDLE_SIZE;
    }

    public static AnalyticsDatasinkConfiguration getInstance() {
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
