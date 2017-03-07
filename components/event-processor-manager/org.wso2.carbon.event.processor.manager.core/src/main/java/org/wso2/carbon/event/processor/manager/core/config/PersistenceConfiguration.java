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
package org.wso2.carbon.event.processor.manager.core.config;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PersistenceConfiguration implements Serializable {
    private String persistenceClass;
    private int threadPoolSize;
    private long persistenceTimeInterval;
    private Map propertiesMap = new HashMap();
    private boolean isPersistenceEnabled;

    public PersistenceConfiguration(String persistenceClass, long persistenceTimeInterval, int threadPoolSize, Map propertiesMap, boolean isPersistenceEnabled) {
        this.persistenceClass = persistenceClass;
        this.persistenceTimeInterval = persistenceTimeInterval;
        this.threadPoolSize = threadPoolSize;
        this.propertiesMap = propertiesMap;
        this.isPersistenceEnabled = isPersistenceEnabled;
    }

    public String getPersistenceClass() {
        return persistenceClass;
    }

    public long getPersistenceTimeInterval() {
        return persistenceTimeInterval;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public Map getPropertiesMap(){
        return propertiesMap;
    }

    public boolean isPersistenceEnabled(){
        return isPersistenceEnabled;
    }

}
