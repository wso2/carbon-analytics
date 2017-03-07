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
package org.wso2.carbon.event.statistics.internal.jmx;

import org.wso2.carbon.event.statistics.EventStatisticsObserver;
import org.wso2.carbon.event.statistics.internal.data.CollectionDTO;
import org.wso2.carbon.event.statistics.internal.data.StatsDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JMXObserver implements EventStatisticsObserver {

    private ConcurrentHashMap<String, List<String>> registeredMbeanIds = new ConcurrentHashMap<String, List<String>>();
    private ConcurrentHashMap<String, Map<String, StatisticsView>> data;

    public JMXObserver() {
        data = new ConcurrentHashMap<String, Map<String, StatisticsView>>();
    }


    public void destroy() {
        for (Map.Entry<String, List<String>> entry : registeredMbeanIds.entrySet()) {
            for (String value : entry.getValue()) {
                MBeanRegistrar.getInstance().unRegisterMBean(entry.getKey(), value);
            }
        }
    }

    @Override
    public void updateStatistics(CollectionDTO collectionDTO) {
        String id = collectionDTO.getStatsDTO().getName();
        updateView(collectionDTO.getStatsDTO(), id);//tenant

        if (collectionDTO.getChildCollectionDTOs() != null) {
            for (CollectionDTO collectionDTO1 : collectionDTO.getChildCollectionDTOs()) {
                String id1 = id + "." + collectionDTO1.getStatsDTO().getName();
                updateView(collectionDTO1.getStatsDTO(), id1);  //category

                if (collectionDTO1.getChildCollectionDTOs() != null) {
                    for (CollectionDTO collectionDTO2 : collectionDTO1.getChildCollectionDTOs()) {
                        String id2 = id1 + "." + collectionDTO2.getStatsDTO().getName();
                        updateView(collectionDTO2.getStatsDTO(), id2);   //deployment

                        if (collectionDTO2.getChildCollectionDTOs() != null) {
                            for (CollectionDTO collectionDTO3 : collectionDTO2.getChildCollectionDTOs()) {
                                String id3 = id2 + "." + collectionDTO3.getStatsDTO().getName();
                                updateView(collectionDTO3.getStatsDTO(), id3);   //element

                            }
                        }
                    }
                }
            }
        }
    }

    private void updateView(StatsDTO statsDTO, String id) {
        Map<String, StatisticsView> map = data.get(statsDTO.getType());
        if (map == null) {
            data.putIfAbsent(statsDTO.getType(), new HashMap<String, StatisticsView>());
            map = data.get(statsDTO.getType());
        }

        StatisticsView view;
        if (!map.containsKey(id)) {
            view = new StatisticsView();
            MBeanRegistrar.getInstance().registerMBean(view,
                                                       statsDTO.getType(),
                                                       id);
            // store this information to unregister the MBeans later
            List<String> ids = registeredMbeanIds.get(statsDTO.getType());
            if (ids == null) {
                registeredMbeanIds.putIfAbsent(statsDTO.getType(),
                                               new ArrayList<String>());
                ids = registeredMbeanIds.get(statsDTO.getType());
            }
            ids.add(id);
            map.put(id, view);
        } else {
            view = map.get(id);
        }

        updateView(view, statsDTO);
    }

    private void updateView(StatisticsView view, StatsDTO statsDTO) {
        view.setRequestCount(view.getRequestCount() + (statsDTO.getRequestTotalCount() - view.getRequestTotalCount()));
        view.setResponseCount(view.getResponseCount() + (statsDTO.getResponseTotalCount() - view.getResponseTotalCount()));

        view.setRequestTotalCount(statsDTO.getRequestTotalCount());
        view.setRequestAvgCountPerSec(statsDTO.getRequestAvgCountPerSec());
        view.setRequestMaxCountPerSec(statsDTO.getRequestMaxCountPerSec());
        view.setRequestLastUpdatedTime(statsDTO.getRequestLastUpdatedTime());

        view.setRequestLastSecCount(statsDTO.getRequestLastSecCount());
        view.setRequestLastMinCount(statsDTO.getRequestLastMinCount());
        view.setRequestLast15MinCount(statsDTO.getRequestLast15MinCount());
        view.setRequestLastHourCount(statsDTO.getRequestLastHourCount());
        view.setRequestLast6HourCount(statsDTO.getRequestLast6HourCount());
        view.setRequestLastDayCount(statsDTO.getRequestLastDayCount());

        view.setResponseTotalCount(statsDTO.getResponseTotalCount());
        view.setResponseAvgCountPerSec(statsDTO.getResponseAvgCountPerSec());
        view.setResponseMaxCountPerSec(statsDTO.getResponseMaxCountPerSec());
        view.setResponseLastUpdatedTime(statsDTO.getResponseLastUpdatedTime());

        view.setResponseLastSecCount(statsDTO.getResponseLastSecCount());
        view.setResponseLastMinCount(statsDTO.getResponseLastMinCount());
        view.setResponseLast15MinCount(statsDTO.getResponseLast15MinCount());
        view.setResponseLastHourCount(statsDTO.getResponseLastHourCount());
        view.setResponseLast6HourCount(statsDTO.getResponseLast6HourCount());
        view.setResponseLastDayCount(statsDTO.getResponseLastDayCount());

    }

}
