/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionContext;
import org.wso2.carbon.analytics.spark.core.AnalyticsServiceHolder;

/**
 * This class represents the analytics spark component.
 * @scr.component name="analytics.spark.component" immediate="true"
 * @scr.reference name="analytics.dataservice" interface="org.wso2.carbon.analytics.dataservice.AnalyticsDataService"
 * cardinality="1..1" policy="dynamic"  bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 */
public class SparkServiceComponent {
    
    private static final Log log = LogFactory.getLog(SparkServiceComponent.class);

    protected void activate(ComponentContext ctx) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting 'SparkServiceComponent'");
            }

            AnalyticsExecutionContext.init();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
    
    public static void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        AnalyticsServiceHolder.setAnalyticsDataService(analyticsDataService);
    }
    
    public static void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        AnalyticsServiceHolder.clearAnalyticsDataService();
    }
    
}
