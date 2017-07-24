/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.axis2.engine.ListenerManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunction;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceService;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This class represents the analytics data service declarative services component.
 *
 * @scr.component name="analytics.component" immediate="true"
 * @scr.reference name="listener.manager.service" interface="org.apache.axis2.engine.ListenerManager"
 * cardinality="1..1" policy="dynamic"  bind="setListenerManager" unbind="unsetListenerManager"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="analytics.datasource.service" interface="org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataSourceService" unbind="unsetAnalyticsDataSourceService"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="analytics.aggregates" interface="org.wso2.carbon.analytics.dataservice.core.indexing.aggregates.AggregateFunction"
 * cardinality="0..n" policy="dynamic" bind="addAggregateFunction" unbind="removeAggregateFunctions"
 * @scr.reference name="datasource.service" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic"  bind="setDataSourceService" unbind="unsetDataSourceService"
 */
public class AnalyticsDataServiceComponent {

    private static final Log log = LogFactory.getLog(AnalyticsDataServiceComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Starting AnalyticsDataServiceComponent#activate");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        try {
            this.loadHazelcast();
            AnalyticsClusterManager clusterManager = new AnalyticsClusterManagerImpl();
            bundleContext.registerService(AnalyticsClusterManager.class, clusterManager, null);
            AnalyticsServiceHolder.setAnalyticsClusterManager(clusterManager);
            AnalyticsDataService analyticsDataService = new AnalyticsDataServiceImpl();
            SecureAnalyticsDataServiceImpl secureAnalyticsDataService = new SecureAnalyticsDataServiceImpl(analyticsDataService);
            AnalyticsDataPurgingDeployer dataPurgingDeployer = new AnalyticsDataPurgingDeployer();
            bundleContext.registerService(AnalyticsDataService.class, analyticsDataService, null);
            bundleContext.registerService(SecureAnalyticsDataService.class, secureAnalyticsDataService, null);
            bundleContext.registerService(AppDeploymentHandler.class.getName(), dataPurgingDeployer, null);
            if (log.isDebugEnabled()) {
                log.debug("Finished AnalyticsDataServiceComponent#activate");
            }
        } catch (Exception e) {
            log.error("Error in activating analytics data service: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        try {
            AnalyticsDataService service = AnalyticsServiceHolder.getAnalyticsDataService();
            if (service != null) {
                service.destroy();
            }
        } catch (Exception e) {
            log.error("Error in deactivating analytics data service: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void loadHazelcast() {
        BundleContext ctx = FrameworkUtil.getBundle(AnalyticsServiceHolder.class).getBundleContext();
        ServiceReference ref = ctx.getServiceReference(HazelcastInstance.class);
        if (ref != null) {
            AnalyticsServiceHolder.setHazelcastInstance((HazelcastInstance) ctx.getService(ref));
        }
    }

    protected void setListenerManager(ListenerManager lm) {
        /* we don't really need this, the listener manager service is acquired
         * to make sure, as a workaround, that the task component is initialized 
         * after the axis2 clustering agent is initialized */
    }

    protected void unsetListenerManager(ListenerManager lm) {
        /* empty */
    }

    protected void setAnalyticsDataSourceService(AnalyticsDataSourceService service) {
        /* just to make sure analytics data source component is initialized first */
    }

    protected void unsetAnalyticsDataSourceService(AnalyticsDataSourceService service) {
        /* empty */
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Setting the Realm Service");
        }
        AnalyticsServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.info("Unsetting the Realm Service");
        }
    }

    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        AnalyticsServiceHolder.setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Datasource Service");
        }
        AnalyticsServiceHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the DataSource Service");
        }
    }

    protected void addAggregateFunction(AggregateFunction aggregateFunction) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Aggregate Function: " + aggregateFunction.getAggregateName());
        }
        AnalyticsServiceHolder.addAggregateFunction(aggregateFunction);
    }

    protected void removeAggregateFunctions(AggregateFunction aggregateFunction) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the aggregate functions..");
        }
        AnalyticsServiceHolder.removeAggregateFunctions();
    }
}
