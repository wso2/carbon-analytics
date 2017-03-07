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

/**
 * MBean for exposing stats over JMX
 */
public interface StatisticsViewMBean {

    public String getRequestLastUpdatedTime();

    public long getRequestTotalCount();

    public long getRequestMaxCountPerSec();

    public double getRequestAvgCountPerSec();

    public long getRequestLastSecCount();

    public long getRequestLastMinCount();

    public long getRequestLast15MinCount();

    public long getRequestLastHourCount();

    public long getRequestLast6HourCount();

    public long getRequestLastDayCount();

    public String getResponseLastUpdatedTime();

    public long getResponseTotalCount();

    public long getResponseMaxCountPerSec();

    public double getResponseAvgCountPerSec();

    public long getResponseLastSecCount();

    public long getResponseLastMinCount();

    public long getResponseLast15MinCount();

    public long getResponseLastHourCount();

    public long getResponseLast6HourCount();

    public long getResponseLastDayCount();

    public long getRequestCount();

    public long getResponseCount();

    public void resetCount();
}
