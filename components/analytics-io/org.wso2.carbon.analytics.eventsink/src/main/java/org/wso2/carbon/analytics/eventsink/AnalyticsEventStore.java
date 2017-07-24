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
package org.wso2.carbon.analytics.eventsink;

import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This is the configuration class which specifies from which streams the tables will be receiving the events,
 * and the relevant table schema.
 *
 */
@XmlRootElement(name = "EventStoreConfiguration")
public class AnalyticsEventStore {

    private String name;
    private String recordStore;
    private EventSource eventSource;
    private boolean mergeSchema;
    private AnalyticsTableSchema analyticsTableSchema;

    @XmlElement(name = "Source")
    public EventSource getEventSource() {
        return eventSource;
    }

    @XmlElement(name = "RecordStoreName")
    public String getRecordStore() {
        return recordStore;
    }

    public void setRecordStore(String recordStore) {
        this.recordStore = recordStore;
    }

    public void setEventSource(EventSource eventSource) throws AnalyticsEventStoreException {
        if (eventSource.getStreamIds().size() > 0) {
            String name = generateName(eventSource.getStreamIds().get(0));
            for (String streamId : eventSource.getStreamIds()) {
                if (!name.equals(generateName(streamId))) {
                    throw new AnalyticsEventStoreException("Only the streams that can be inserted into same" +
                            " table can be accepted! Expected table name : " + name + ", but stream id : " + streamId + " is not valid.");
                }
            }
            this.eventSource = eventSource;
            this.name = name;
        } else {
            throw new AnalyticsEventStoreException("Event source should contain atleast one stream, " +
                    "but it's empty!");
        }
    }

    @XmlElement(name = "TableSchema")
    public AnalyticsTableSchema getAnalyticsTableSchema() {
        return analyticsTableSchema;
    }

    public void setAnalyticsTableSchema(AnalyticsTableSchema analyticsTableSchema) {
        this.analyticsTableSchema = analyticsTableSchema;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof AnalyticsEventStore) {
            AnalyticsEventStore analyticsEventStore = (AnalyticsEventStore) object;
            return analyticsEventStore.getName().equalsIgnoreCase(this.getName());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    private String generateName(String streamId) {
        String streamName = streamId.split(":")[0].trim();
        String tableName = "";
        if (!streamName.isEmpty()) {
            tableName = streamName.replace('.', '_');
        }
        return tableName;
    }

    @XmlElement(name = "MergeSchema")
    public boolean isMergeSchema() {
        return mergeSchema;
    }

    public void setMergeSchema(boolean isMergeSchema) {
        this.mergeSchema = isMergeSchema;
    }

    public static class EventSource {
        private List<String> streamIds;

        @XmlElement(name = "StreamId")
        public List<String> getStreamIds() {
            return streamIds;
        }

        public void setStreamIds(List<String> streamIds) {
            this.streamIds = streamIds;
        }

        public boolean contains(String streamId) {
            return streamIds != null && streamIds.contains(streamId);
        }
    }

}
