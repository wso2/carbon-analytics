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

import java.util.concurrent.ConcurrentHashMap;

public class StatsCounter {

    private BasicStatsCounter requestStatCounter = new BasicStatsCounter();
    private BasicStatsCounter responseStatCounter = new BasicStatsCounter();

    private ConcurrentHashMap<String, StatsCounter> childCounters = new ConcurrentHashMap<String, StatsCounter>();
    private String name;
    private String type;

    public StatsCounter(String name, String type) {
        this.name = name;
        this.type = type;
    }


    public void addChildCounter(String name, StatsCounter childCounter) {
        this.childCounters.putIfAbsent(name, childCounter);
    }

    public StatsCounter getChildCounter(String name) {
        return this.childCounters.get(name);
    }

    public String getName() {
        return name;
    }

    public void incrementRequest() {
        requestStatCounter.update();
    }

    public void incrementResponse() {
        responseStatCounter.update();
    }

    public void reset() {
        requestStatCounter.reset();
        responseStatCounter.reset();
        for (StatsCounter childStatsCounter : childCounters.values()) {
            childStatsCounter.reset();
        }

    }

    public BasicStatsCounter getRequestStatCounter() {
        return requestStatCounter;
    }

    public BasicStatsCounter getResponseStatCounter() {
        return responseStatCounter;
    }

    public ConcurrentHashMap<String, StatsCounter> getChildCounters() {
        return childCounters;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
