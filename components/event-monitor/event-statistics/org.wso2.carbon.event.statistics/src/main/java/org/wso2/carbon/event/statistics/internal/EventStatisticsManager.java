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
package org.wso2.carbon.event.statistics.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.statistics.EventStatisticsMonitor;
import org.wso2.carbon.event.statistics.EventStatisticsObserver;
import org.wso2.carbon.event.statistics.internal.counter.StatsCounter;
import org.wso2.carbon.event.statistics.internal.data.CollectionDTO;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventStatisticsManager {

    private static final Log log = LogFactory.getLog(EventStatisticsManager.class);

    ConcurrentHashMap<Integer, StatsCounter> tenantDataMap;
    private Set<EventStatisticsObserver> observers = new HashSet<EventStatisticsObserver>();
    private final ConcurrentHashMap<String, EventStatisticsMonitor> statisticsMonitorMap;

    public EventStatisticsManager() {
        this.tenantDataMap = new ConcurrentHashMap<Integer, StatsCounter>();
        statisticsMonitorMap = new ConcurrentHashMap<String, EventStatisticsMonitor>();
    }

    private synchronized EventStatisticsMonitor createNewEventStatisticMonitor(int tenantId, String category, String deployment, String element) {

        StatsCounter tenantData = tenantDataMap.get(tenantId);
        if (tenantData == null) {
            tenantData = new StatsCounter(tenantId + "", org.wso2.carbon.event.statistics.internal.Constants.TENANT);
            tenantDataMap.putIfAbsent(tenantId, tenantData);
            tenantDataMap.putIfAbsent(tenantId, tenantData);
            tenantData = tenantDataMap.get(tenantId);
        }

        StatsCounter categoryData = tenantData.getChildCounter(category);
        if (categoryData == null) {
            categoryData = new StatsCounter(category, org.wso2.carbon.event.statistics.internal.Constants.TENANT);
            tenantData.addChildCounter(category, categoryData);
            categoryData = tenantData.getChildCounter(category);
        }


        StatsCounter deploymentData = categoryData.getChildCounter(deployment);
        if (deploymentData == null) {
            deploymentData = new StatsCounter(deployment, org.wso2.carbon.event.statistics.internal.Constants.TENANT);
            categoryData.addChildCounter(deployment, deploymentData);
            deploymentData = categoryData.getChildCounter(deployment);
        }


        StatsCounter elementData = null;
        if (element != null) {
            elementData = deploymentData.getChildCounter(element);
            if (elementData == null) {
                elementData = new StatsCounter(element, org.wso2.carbon.event.statistics.internal.Constants.TENANT);
                deploymentData.addChildCounter(element, elementData);
                elementData = deploymentData.getChildCounter(element);
            }
        }

        return new EventStatisticsMonitorImpl(tenantData, categoryData, deploymentData, elementData);

    }

    public synchronized EventStatisticsMonitor getEventStatisticMonitor(int tenantId, String category, String deployment, String element) {

        String key = String.valueOf(tenantId) + category + deployment + element;
        EventStatisticsMonitor statisticsMonitor = statisticsMonitorMap.get(key);
        if (statisticsMonitor == null) {
            synchronized (this) {
                statisticsMonitor = statisticsMonitorMap.get(key);
                if (statisticsMonitor == null) {
                    statisticsMonitor = createNewEventStatisticMonitor(tenantId, category, deployment, element);
                    statisticsMonitorMap.put(key, statisticsMonitor);
                }
            }
        }
        return statisticsMonitor;
    }

    public synchronized void reset() {
        for (StatsCounter tenantData : tenantDataMap.values()) {
            tenantData.reset();
        }
    }

    public ConcurrentHashMap<Integer, StatsCounter> getTenantDataMap() {
        return tenantDataMap;
    }

    /**
     * Register a custom statistics consumer to receive updates from this
     * statistics store
     *
     * @param o The EventStatisticsObserver instance to be notified of data updates
     */
    public void registerObserver(EventStatisticsObserver o) {
        observers.add(o);
    }

    /**
     * Unregister the custom statistics consumer from the mediation statistics store
     *
     * @param o The EventStatisticsObserver instance to be removed
     */
    public void unregisterObserver(EventStatisticsObserver o) {
        if (observers.contains(o)) {
            observers.remove(o);
            o.destroy();
        }
    }

    public void unregisterObservers() {
        if (log.isDebugEnabled()) {
            log.debug("Unregistering event statistics observers");
        }

        for (EventStatisticsObserver o : observers) {
            o.destroy();
        }
        observers.clear();
    }

    public void updateStatistics(CollectionDTO collectionDTO) {

        for (EventStatisticsObserver o : observers) {
            try {
                o.updateStatistics(collectionDTO);
            } catch (Throwable t) {
                log.error("Error occurred while notifying the event statistics observer", t);
            }
        }
    }


}
