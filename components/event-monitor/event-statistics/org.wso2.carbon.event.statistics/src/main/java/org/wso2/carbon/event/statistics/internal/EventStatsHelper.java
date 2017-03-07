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

import org.wso2.carbon.event.statistics.internal.counter.StatsCounter;
import org.wso2.carbon.event.statistics.internal.data.StatsDTO;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

public class EventStatsHelper {

    public static StatsDTO constructStatsDTO(StatsCounter statsCounter) {
        StatsDTO statsDTO = new StatsDTO();
        statsDTO.setName(statsCounter.getName());
        statsDTO.setType(statsCounter.getType());
        statsDTO.setRequestTotalCount(statsCounter.getRequestStatCounter().getTotalCount());
        statsDTO.setRequestAvgCountPerSec(statsCounter.getRequestStatCounter().getAvgCountPerSec());
        statsDTO.setRequestMaxCountPerSec(statsCounter.getRequestStatCounter().getMaxCountPerSec());
        statsDTO.setRequestLastUpdatedTime(DateFormat.getDateTimeInstance().format(new Date(statsCounter.getRequestStatCounter().getLastUpdatedTime())));

        statsDTO.setRequestLastSecCount(statsCounter.getRequestStatCounter().getLastSecCount());
        statsDTO.setRequestLastMinCount(statsCounter.getRequestStatCounter().getLastMinCount());
        statsDTO.setRequestLast15MinCount(statsCounter.getRequestStatCounter().getLast15MinCount());
        statsDTO.setRequestLastHourCount(statsCounter.getRequestStatCounter().getLastHourCount());
        statsDTO.setRequestLast6HourCount(statsCounter.getRequestStatCounter().getLast6HourCount());
        statsDTO.setRequestLastDayCount(statsCounter.getRequestStatCounter().getLastDayCount());

        statsDTO.setResponseTotalCount(statsCounter.getResponseStatCounter().getTotalCount());
        statsDTO.setResponseAvgCountPerSec(statsCounter.getResponseStatCounter().getAvgCountPerSec());
        statsDTO.setResponseMaxCountPerSec(statsCounter.getResponseStatCounter().getMaxCountPerSec());
        statsDTO.setResponseLastUpdatedTime(DateFormat.getDateTimeInstance().format(new Date(statsCounter.getResponseStatCounter().getLastUpdatedTime())));

        statsDTO.setResponseLastSecCount(statsCounter.getResponseStatCounter().getLastSecCount());
        statsDTO.setResponseLastMinCount(statsCounter.getResponseStatCounter().getLastMinCount());
        statsDTO.setResponseLast15MinCount(statsCounter.getResponseStatCounter().getLast15MinCount());
        statsDTO.setResponseLastHourCount(statsCounter.getResponseStatCounter().getLastHourCount());
        statsDTO.setResponseLast6HourCount(statsCounter.getResponseStatCounter().getLast6HourCount());
        statsDTO.setResponseLastDayCount(statsCounter.getResponseStatCounter().getLastDayCount());


        if (statsCounter.getChildCounters().size() > 0) {
            String[] children = statsCounter.getChildCounters().keySet().toArray(new String[statsCounter.getChildCounters().keySet().size()]);
            Arrays.sort(children);
            statsDTO.setChildStats(children);
        }

        return statsDTO;
    }
}
