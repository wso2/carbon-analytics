/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.eventsink.internal;

import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.bind.*;
import java.io.File;
import java.util.*;

public class AnalyticsEventStoreManager {

    private static AnalyticsEventStoreManager instance = new AnalyticsEventStoreManager();

    private Map<Integer, Set<AnalyticsEventStore>> tenantEventStoreMap;

    private AnalyticsEventStoreManager() {
        this.tenantEventStoreMap = new HashMap<>();
    }

    public static AnalyticsEventStoreManager getInstance() {
        return instance;
    }

    public void addEventStoreConfiguration(int tenantId,
                                           AnalyticsEventStore analyticsEventStore)
            throws AnalyticsEventStoreException {
        Set<AnalyticsEventStore> analyticsEventStoreList = tenantEventStoreMap.get(tenantId);
        if (analyticsEventStoreList == null) {
            synchronized (this) {
                analyticsEventStoreList = tenantEventStoreMap.get(tenantId);
                if (analyticsEventStoreList == null) {
                    analyticsEventStoreList = new LinkedHashSet<>();
                }
            }
        }
        analyticsEventStoreList.add(analyticsEventStore);
    }

    public void saveEventStoreConfiguration(int tenantId, AnalyticsEventStore eventStore)
            throws AnalyticsEventStoreException {
        String fileName = MultitenantUtils.getAxis2RepositoryPath(tenantId) + File.separator + eventStore.getName();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(AnalyticsEventSinkConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(eventStore, new File(fileName));
        } catch (JAXBException e) {
            throw new AnalyticsEventStoreException("Error while marshalling the analytics event store " +
                    "configuration at file : " + fileName, e);
        }
    }

    public void deleteEventStoreConfiguration(int tenantId, String eventStoreName) {
        File eventStoreFile = new File(MultitenantUtils.getAxis2RepositoryPath(tenantId)
                + File.separator + eventStoreName);
        eventStoreFile.deleteOnExit();
    }

    public AnalyticsEventStore getAnalyticsEventStore(File configFile)
            throws AnalyticsEventStoreException {
        try {
            JAXBContext context = JAXBContext.newInstance(AnalyticsEventStore.class);
            Unmarshaller un = context.createUnmarshaller();
            return (AnalyticsEventStore) un.unmarshal(configFile);
        } catch (JAXBException e) {
            throw new AnalyticsEventStoreException("Error while unmarshalling the configuration from file : "
                    + configFile.getPath(), e);
        }
    }

    public AnalyticsEventStore getAnalyticsEventStore(int tenantId, String eventStoreName) {
        Set<AnalyticsEventStore> eventStoreConfigurations = tenantEventStoreMap.get(tenantId);
        if (eventStoreConfigurations != null) {
            for (AnalyticsEventStore configuration : eventStoreConfigurations) {
                if (configuration.getName().equals(eventStoreName)) {
                    return configuration;
                }
            }
        }
        return null;
    }


    public AnalyticsEventStore removeEventStoreConfiguration(int tenantId, String eventStoreName) {
        Set<AnalyticsEventStore> analyticsEventStoreList = tenantEventStoreMap.get(tenantId);
        if (analyticsEventStoreList != null) {
            for (AnalyticsEventStore analyticsEventStore : analyticsEventStoreList) {
                if (analyticsEventStore.getName().equals(eventStoreName)) {
                     analyticsEventStoreList.remove(analyticsEventStore);
                    return analyticsEventStore;
                }
            }
        }
        return null;
    }
}
