/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.statistics;


import org.wso2.carbon.event.statistics.internal.Constants;
import org.wso2.carbon.event.statistics.internal.EventStatisticsManager;
import org.wso2.carbon.event.statistics.internal.EventStatsHelper;
import org.wso2.carbon.event.statistics.internal.GhostEventStatisticsMonitor;
import org.wso2.carbon.event.statistics.internal.counter.StatsCounter;
import org.wso2.carbon.event.statistics.internal.data.StatsDTO;
import org.wso2.carbon.event.statistics.internal.ds.EventStatisticsServiceHolder;

public class EventStatisticsService {


    public StatsDTO getGlobalCount(int tenantId) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        if (eventStatisticsManager == null) {
            return null;
        }
        StatsCounter tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);

        if (tenantData == null) {
            tenantData = new StatsCounter(tenantId + "", Constants.TENANT);
            eventStatisticsManager.getTenantDataMap().putIfAbsent(tenantId, tenantData);
            tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);
        }

        return EventStatsHelper.constructStatsDTO(tenantData);
    }

    public StatsDTO getCategoryCount(int tenantId, String categoryName) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        if (eventStatisticsManager == null) {
            return null;
        }
        StatsCounter tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);

        if (tenantData == null) {
            tenantData = new StatsCounter(tenantId + "", Constants.TENANT);
            eventStatisticsManager.getTenantDataMap().putIfAbsent(tenantId, tenantData);
            tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);
        }

        StatsCounter categoryData = tenantData.getChildCounter(categoryName);

        if (categoryData == null) {
            categoryData = new StatsCounter(categoryName, Constants.CATEGORY);
            tenantData.getChildCounters().putIfAbsent(categoryName, categoryData);
            categoryData = tenantData.getChildCounters().get(categoryName);
        }

        return EventStatsHelper.constructStatsDTO(categoryData);
    }

    public StatsDTO getDeploymentCount(int tenantId, String categoryName, String deploymentName) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        if (eventStatisticsManager == null) {
            return null;
        }
        StatsCounter tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);

        if (tenantData == null) {
            tenantData = new StatsCounter(tenantId + "", Constants.TENANT);
            eventStatisticsManager.getTenantDataMap().putIfAbsent(tenantId, tenantData);
            tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);
        }

//        StatsCounter categoryData = getChildStatsCounter(categoryName, tenantData, Constants.CATEGORY);
//        StatsCounter deploymentData = getChildStatsCounter(deploymentName, categoryData, Constants.DEPLOYMENT);

        StatsCounter categoryData = tenantData.getChildCounter(categoryName);
        if (categoryData == null) {
            return null;
        }
        StatsCounter deploymentData = categoryData.getChildCounter(deploymentName);
        if (deploymentData == null) {
            return null;
        }

        return EventStatsHelper.constructStatsDTO(deploymentData);
    }

    public StatsDTO getElementCount(int tenantId, String categoryName, String deploymentName, String elementName) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        if (eventStatisticsManager == null) {
            return null;
        }
        StatsCounter tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);

        if (tenantData == null) {
            tenantData = new StatsCounter(tenantId + "", Constants.TENANT);
            eventStatisticsManager.getTenantDataMap().putIfAbsent(tenantId, tenantData);
            tenantData = eventStatisticsManager.getTenantDataMap().get(tenantId);
        }

//        StatsCounter categoryData = getChildStatsCounter(categoryName, tenantData, Constants.CATEGORY);
//        StatsCounter deploymentData = getChildStatsCounter(deploymentName, categoryData, Constants.DEPLOYMENT);
//        StatsCounter elementData = getChildStatsCounter(elementName, deploymentData, Constants.ELEMENT);
//
        StatsCounter categoryData = tenantData.getChildCounter(categoryName);
        if (categoryData == null) {
            return null;
        }
        StatsCounter deploymentData = categoryData.getChildCounter(deploymentName);
        if (deploymentData == null) {
            return null;
        }
        StatsCounter elementData = deploymentData.getChildCounter(elementName);
        if (elementData == null) {
            return null;
        }

        return EventStatsHelper.constructStatsDTO(elementData);
    }


//    private StatsCounter getChildStatsCounter(String childName, StatsCounter parentStatsCounter, String type) {
//        StatsCounter data = parentStatsCounter.getChildCounter(childName);
//
//        if (data == null) {
//            data = new StatsCounter(childName, type);
//            parentStatsCounter.getChildCounters().putIfAbsent(childName, data);
//            data = parentStatsCounter.getChildCounters().get(childName);
//        }
//        return data;
//    }


    public synchronized EventStatisticsMonitor getEventStatisticMonitor(int tenantId, String category, String deployment, String element) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        if (eventStatisticsManager == null) {
            return new GhostEventStatisticsMonitor();
        }
        return eventStatisticsManager.getEventStatisticMonitor(tenantId, category, deployment, element);
    }

    public synchronized void reset() {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        eventStatisticsManager.reset();
    }

    /**
     * Register a custom statistics consumer to receive updates from this
     * statistics store
     *
     * @param o The EventStatisticsObserver instance to be notified of data updates
     */
    public void registerObserver(EventStatisticsObserver o) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        eventStatisticsManager.registerObserver(o);
    }

    /**
     * Unregister the custom statistics consumer from the mediation statistics store
     *
     * @param o The EventStatisticsObserver instance to be removed
     */
    public void unregisterObserver(EventStatisticsObserver o) {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        eventStatisticsManager.unregisterObserver(o);
    }

    public void unregisterObservers() {
        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();
        eventStatisticsManager.unregisterObservers();
    }

}