/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.engine.services;

import org.wso2.carbon.analytics.data.commons.AnalyticsDataService;
import org.wso2.carbon.analytics.engine.commons.AnalyticsIncrementalMetaStore;
import org.wso2.carbon.analytics.engine.exceptions.AnalyticsDataServiceLoadException;

import java.util.ServiceLoader;

/**
 * Provides Analytics services to the Analyzer.
 */
public class AnalyticsServiceHolder {

    private static AnalyticsDataService analyticsDataService;
    private static AnalyticsIncrementalMetaStore incrementalMetaStore;
    private static ServiceLoader<AnalyticsDataService> analyticsDataServiceServiceLoader;

    static {
        analyticsDataServiceServiceLoader = ServiceLoader.load(AnalyticsDataService.class);
    }

    /**
     * Returns the Analytics Data Service loaded through Java SPI.
     * @return analyticsDataService object
     * @throws AnalyticsDataServiceLoadException if the class cannot be loaded
     */
    public static synchronized AnalyticsDataService getAnalyticsDataService() throws AnalyticsDataServiceLoadException {
        if (analyticsDataService == null) {
            analyticsDataService = analyticsDataServiceServiceLoader.iterator().next();
            if (analyticsDataService == null) {
                throw new AnalyticsDataServiceLoadException("Analytics Data Service cannot be loaded!");
            }
        }
        return analyticsDataService;
    }

    /**
     * Get the Analytics Incremental Meta Store.
     * @return Analytics Incremental Meta Store
     */
    public static AnalyticsIncrementalMetaStore getIncrementalMetaStore() {
        if (incrementalMetaStore == null) {
            try {
                incrementalMetaStore = new AnalyticsIncrementalMetaStore();
            } catch (Exception e) {
                throw new RuntimeException("Error in creating analytics incremental metastore: " + e.getMessage(), e);
            }
        }
        return incrementalMetaStore;
    }
}
