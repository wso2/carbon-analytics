/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dataservice.core;

import com.hazelcast.core.HazelcastInstance;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunction;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents a service holder class for analytics data service.
 */
public class AnalyticsServiceHolder {

    public static final String FORCE_INDEXING_ENV_PROP = "force.indexing";

    private static HazelcastInstance hazelcastInstance;

    private static AnalyticsClusterManager analyticsClusterManager;

    private static volatile AnalyticsDataService analyticsDataService;

    private static RealmService realmService;

    private static TaskService taskService;

    private static DataSourceService dataSourceService;

    private static Map<String, Class<? extends AggregateFunction>> aggregateFunctions = new HashMap<>();

    public static void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        AnalyticsServiceHolder.hazelcastInstance = hazelcastInstance;
    }

    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public static AnalyticsClusterManager getAnalyticsClusterManager() {
        return analyticsClusterManager;
    }

    public static void setAnalyticsClusterManager(AnalyticsClusterManager analyticsClusterManager) {
        AnalyticsServiceHolder.analyticsClusterManager = analyticsClusterManager;
    }

    public static AnalyticsDataService getAnalyticsDataService() {
        if (analyticsDataService == null) {
            synchronized (AnalyticsServiceHolder.class) {
                if (analyticsDataService == null) {
                    checkAndPopulateCustomAnalyticsDS();
                }
            }
        }
        return analyticsDataService;
    }

    private static void checkAndPopulateCustomAnalyticsDS() {
        try {
            if (System.getProperty(FORCE_INDEXING_ENV_PROP) == null) {
                System.setProperty(Constants.DISABLE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
            }
            analyticsClusterManager = new AnalyticsClusterManagerImpl();
            analyticsDataService = new AnalyticsDataServiceImpl();
        } catch (AnalyticsException e) {
            throw new RuntimeException("Error in creating analytics data service impl.: " + e.getMessage(), e);
        }
    }

    public static void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        AnalyticsServiceHolder.analyticsDataService = analyticsDataService;
    }

    public static void setRealmService(RealmService realmService) {
        AnalyticsServiceHolder.realmService = realmService;
    }

    public static RealmService getRealmService() {
        return AnalyticsServiceHolder.realmService;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static void setTaskService(TaskService taskService) {
        AnalyticsServiceHolder.taskService = taskService;
    }

    public static void setDataSourceService(DataSourceService dataSourceService) {
        AnalyticsServiceHolder.dataSourceService = dataSourceService;
    }

    public  static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void addAggregateFunction(AggregateFunction aggregateFunction) {
        if (aggregateFunction.getAggregateName() != null && !aggregateFunction.getAggregateName().isEmpty()) {
            AnalyticsServiceHolder.aggregateFunctions.put(aggregateFunction.getAggregateName(), aggregateFunction.getClass());
        } else {
            throw new RuntimeException("Error while reading AggregateFunctions as OSGI Components: " +
                                       "AggregateFunction.getAggregateName() is not properly implemented " +
                                       "(return null or return empty String");
        }
    }

    public static void removeAggregateFunctions() {
        AnalyticsServiceHolder.aggregateFunctions = null;
    }

    public static Map<String, Class<? extends AggregateFunction>> getAggregateFunctions() {
        return AnalyticsServiceHolder.aggregateFunctions;
    }
    
}
