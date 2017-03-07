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

import org.apache.log4j.Logger;
import org.wso2.carbon.event.statistics.internal.counter.StatsCounter;
import org.wso2.carbon.event.statistics.internal.data.CollectionDTO;
import org.wso2.carbon.event.statistics.internal.ds.EventStatisticsServiceHolder;

public class StatisticsReporterThread extends Thread {

    private static Logger log = Logger.getLogger(StatisticsReporterThread.class);

    private boolean shutdownRequested = false;

    private long delay = 5 * 1000;

    private int tenantId;

    public StatisticsReporterThread(int tenantId) {
        this.setName("event-stat-collector-" + tenantId);
        this.tenantId = tenantId;
    }

    public void setDelay(long delay) {
        if (log.isDebugEnabled()) {
            log.debug("Event statistics reporter delay set to " + delay + " ms");
        }
        this.delay = delay;
    }


    private void delay() {
        if (delay <= 0) {
            return;
        }

        try {
            sleep(delay);
        } catch (InterruptedException ignore) {

        }
    }

    public void run() {
        while (!shutdownRequested) {
            try {
                collectDataAndReport();
            } catch (Throwable t) {
                // catch all possible errors to prevent the thread from dying
                log.error("Error while collecting and reporting event statistics", t);
            }
        }
    }

    private void collectDataAndReport() {
        if (log.isDebugEnabled()) {
            log.trace("Starting new mediation statistics collection cycle");
        }

        EventStatisticsManager eventStatisticsManager = EventStatisticsServiceHolder.getInstance().getEventStatisticsManager();

        CollectionDTO tenantCollectionDTO = new CollectionDTO();

        StatsCounter tenantCounter = eventStatisticsManager.getTenantDataMap().get(tenantId);

        if (tenantCounter == null) {
            tenantCounter = new StatsCounter(tenantId + "", Constants.TENANT);
            eventStatisticsManager.getTenantDataMap().putIfAbsent(tenantId, tenantCounter);
            tenantCounter = eventStatisticsManager.getTenantDataMap().get(tenantId);
        }

        tenantCollectionDTO.setStatsDTO(EventStatsHelper.constructStatsDTO(tenantCounter));

        if (tenantCounter.getChildCounters().size() > 0) {
            CollectionDTO[] categoryCollectionDTOs = new CollectionDTO[tenantCounter.getChildCounters().size()];
            tenantCollectionDTO.setChildCollectionDTOs(categoryCollectionDTOs);
            int categoryCount = 0;
            for (StatsCounter categoryCounter : tenantCounter.getChildCounters().values()) {
                categoryCollectionDTOs[categoryCount] = new CollectionDTO();
                categoryCollectionDTOs[categoryCount].setStatsDTO(EventStatsHelper.constructStatsDTO(categoryCounter));
                if (categoryCounter.getChildCounters().size() > 0) {
                    CollectionDTO[] deploymentCollectionDTOs = new CollectionDTO[categoryCounter.getChildCounters().size()];
                    categoryCollectionDTOs[categoryCount].setChildCollectionDTOs(deploymentCollectionDTOs);
                    int deploymentCount = 0;
                    for (StatsCounter deploymentCounter : categoryCounter.getChildCounters().values()) {
                        deploymentCollectionDTOs[deploymentCount] = new CollectionDTO();
                        deploymentCollectionDTOs[deploymentCount].setStatsDTO(EventStatsHelper.constructStatsDTO(deploymentCounter));
                        if (deploymentCounter.getChildCounters().size() > 0) {
                            CollectionDTO[] elementCollectionDTOs = new CollectionDTO[deploymentCounter.getChildCounters().size()];
                            deploymentCollectionDTOs[deploymentCount].setChildCollectionDTOs(elementCollectionDTOs);
                            int elementCount = 0;
                            for (StatsCounter elementCounter : deploymentCounter.getChildCounters().values()) {
                                elementCollectionDTOs[elementCount] = new CollectionDTO();
                                elementCollectionDTOs[elementCount].setStatsDTO(EventStatsHelper.constructStatsDTO(elementCounter));
                                elementCount++;
                            }
                        }
                        deploymentCount++;
                    }
                }
                categoryCount++;
            }
        }
        eventStatisticsManager.updateStatistics(tenantCollectionDTO);

        delay();

    }

    public void shutdown() {
        if (log.isDebugEnabled()) {
            log.debug("Event statistics reporter thread is being stopped");
        }
        shutdownRequested = true;
    }
}
