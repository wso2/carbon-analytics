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
package org.wso2.carbon.event.statistics.internal.counter;

import org.wso2.carbon.event.statistics.internal.Constants;

public class BasicStatsCounter {
    private long startTime;
    private long lastUpdatedTime;

    private long totalCount;

    private long prevSecCount;
    private long prevMinCount;
    private long prev15MinCount;
    private long prevHourCount;
    private long prev6HourCount;
    private long prevDayCount;

    private long maxCountPerSec;
    private double avgCountPerSec;

    private long currentSecCount;
    private long currentMinCount;
    private long current15MinCount;
    private long currentHourCount;
    private long current6HourCount;
    private long currentDayCount;

    private long currentSec;
    private long currentMin;
    private long current15Min;
    private long currentHour;
    private long current6Hour;
    private long currentDay;

    public BasicStatsCounter() {
        reset();
    }


    public long getTotalCount() {
        return totalCount;
    }

    public long getLastSecCount() {
        return currentSecCount + (prevSecCount * (Constants.SEC_IN_MS - (lastUpdatedTime - currentSec * Constants.SEC_IN_MS))) / Constants.SEC_IN_MS;
    }

    public long getLastMinCount() {
        return currentMinCount + (prevMinCount * (Constants.MIN_IN_MS - (lastUpdatedTime - currentMin * Constants.MIN_IN_MS))) / Constants.MIN_IN_MS;
    }

    public long getLast15MinCount() {
        return current15MinCount + (prev15MinCount * (Constants.MIN15_IN_MS - (lastUpdatedTime - current15Min * Constants.MIN15_IN_MS))) / Constants.MIN15_IN_MS;
    }

    public long getLastHourCount() {
        return currentHourCount + (prevHourCount * (Constants.HOUR_IN_MS - (lastUpdatedTime - currentHour * Constants.HOUR_IN_MS))) / Constants.HOUR_IN_MS;
    }

    public long getLast6HourCount() {
        return current6HourCount + (prev6HourCount * (Constants.HOUR6_IN_MS - (lastUpdatedTime - current6Hour * Constants.HOUR6_IN_MS))) / Constants.HOUR6_IN_MS;
    }

    public long getLastDayCount() {
        return currentDayCount + (prevDayCount * (Constants.DAY_IN_MS - (lastUpdatedTime - currentDay * Constants.DAY_IN_MS))) / Constants.DAY_IN_MS;
    }

    public long getMaxCountPerSec() {
        return maxCountPerSec;
    }

    public double getAvgCountPerSec() {
        return Math.round(avgCountPerSec * 1000) / 1000.0;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public synchronized void update() {
        lastUpdatedTime = System.currentTimeMillis();
        totalCount++;

        if (currentSec < lastUpdatedTime / Constants.SEC_IN_MS) {
            currentSec = lastUpdatedTime / Constants.SEC_IN_MS;
            prevSecCount = currentSecCount;
            currentSecCount = 0l;

            if (currentMin < lastUpdatedTime / Constants.MIN_IN_MS) {
                currentMin = lastUpdatedTime / Constants.MIN_IN_MS;
                prevMinCount = currentMinCount;
                currentMinCount = 0l;

                if (current15Min < lastUpdatedTime / Constants.MIN15_IN_MS) {
                    current15Min = lastUpdatedTime / Constants.MIN15_IN_MS;
                    prev15MinCount = current15MinCount;
                    current15MinCount = 0l;
                }
                if (currentHour < lastUpdatedTime / Constants.HOUR_IN_MS) {
                    currentHour = lastUpdatedTime / Constants.HOUR_IN_MS;
                    prevHourCount = currentHourCount;
                    currentHourCount = 0l;
                }

                if (current6Hour < lastUpdatedTime / Constants.HOUR6_IN_MS) {
                    current6Hour = lastUpdatedTime / Constants.HOUR6_IN_MS;
                    prev6HourCount = current6HourCount;
                    current6HourCount = 0l;
                }
                if (currentDay < lastUpdatedTime / Constants.DAY_IN_MS) {
                    currentDay = lastUpdatedTime / Constants.DAY_IN_MS;
                    prevDayCount = currentDayCount;
                    currentDayCount = 0l;
                }
            }

        }


        currentSecCount++;
        currentMinCount++;
        current15MinCount++;
        currentHourCount++;
        current6HourCount++;
        currentDayCount++;

        if (currentSecCount > maxCountPerSec) {
            maxCountPerSec = currentSecCount;
        }
        if (currentSec == startTime / Constants.SEC_IN_MS) {
            startTime = lastUpdatedTime;
            avgCountPerSec = totalCount;
        } else {
            avgCountPerSec = totalCount * ((Constants.SEC_IN_MS * 1.0) / (lastUpdatedTime - startTime));
        }
    }

    @Override
    public String toString() {
        return "DataCounter{" +
               "totalCount=" + getTotalCount() +
               ", avgCountPerSec=" + getAvgCountPerSec() +
               ", maxCountPerSec=" + getMaxCountPerSec() +
               ", lastSecCount=" + getLastSecCount() +
               ", lastMinCount=" + getLastMinCount() +
               ", last15MinCount=" + getLast15MinCount() +
               ", lastHourCount=" + getLastHourCount() +
               ", last6HourCount=" + getLast6HourCount() +
               ", lastDayCount=" + getLastDayCount() +
               '}';
    }

    public void reset() {

        this.startTime = System.currentTimeMillis();
        lastUpdatedTime = -1l;

        totalCount = 0l;

        prevSecCount = 0l;
        prevMinCount = 0l;
        prev15MinCount = 0l;
        prevHourCount = 0l;
        prev6HourCount = 0l;
        prevDayCount = 0l;

        maxCountPerSec = 0l;
        avgCountPerSec = 0.0;

        currentSecCount = 0l;
        currentMinCount = 0l;
        current15MinCount = 0l;
        currentHourCount = 0l;
        current6HourCount = 0l;
        currentDayCount = 0l;

        currentSec = 0l;
        currentMin = 0l;
        current15Min = 0l;
        currentHour = 0l;
        current6Hour = 0l;
        currentDay = 0l;

    }
}
