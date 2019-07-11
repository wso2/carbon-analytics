/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package util;

import org.wso2.carbon.streaming.integrator.common.EventStreamService;
import org.wso2.carbon.streaming.integrator.common.exception.ResourceNotFoundException;
import io.siddhi.core.event.Event;
import io.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class StreamProcessorUtil implements EventStreamService {
    private HashMap<String, HashMap<String, List<Attribute>>> streamAttributesMap = new HashMap<>();
    private LinkedList<EventData> eventsReceived = new LinkedList<>();

    public StreamProcessorUtil() { }

    @Override
    public List<String> getStreamNames(String streamName) {
        List<String> streamNames = new ArrayList<>();
        streamAttributesMap.forEach((siddhiApp, streamMap) -> {
            if (streamMap.containsKey(streamName)) {
                streamMap.get(streamName).forEach(attribute -> streamNames.add(attribute.getName()));
            }
        });
        return streamNames;
    }

    @Override
    public List<Attribute> getStreamAttributes(String siddhiAppName, String streamName)
            throws ResourceNotFoundException {
        if (streamAttributesMap.containsKey(siddhiAppName)) {
            if (streamAttributesMap.get(siddhiAppName).containsKey(streamName)) {
                return streamAttributesMap.get(siddhiAppName).get(streamName);
            } else {
                throw new ResourceNotFoundException("Siddhi app '" + siddhiAppName + "' does not contain " +
                        "stream '" + streamName + "'.", ResourceNotFoundException.ResourceType.STREAM_NAME,
                        streamName);
            }
        } else {
            throw new ResourceNotFoundException("Siddhi app '" + siddhiAppName + "' does not exist.",
                    ResourceNotFoundException.ResourceType.SIDDHI_APP_NAME, siddhiAppName);
        }
    }

    @Override
    public void pushEvent(String siddhiAppName, String streamName, Event event) {
        eventsReceived.add(new EventData(siddhiAppName, streamName, event));
    }

    public int getNoOfEvents() {
        return eventsReceived.size();
    }

    public HashMap<String, HashMap<String, List<Attribute>>> getStreamAttributesMap() {
        return streamAttributesMap;
    }

    public void setStreamAttributesMap(HashMap<String, HashMap<String, List<Attribute>>> streamAttributesMap) {
        this.streamAttributesMap = streamAttributesMap;
    }

    public LinkedList<EventData> getEventsReceived() {
        return eventsReceived;
    }

    public void resetEvents() {
        eventsReceived.clear();
    }

    public void setEventsReceived(LinkedList<EventData> eventsReceived) {
        this.eventsReceived = eventsReceived;
    }

    public void addStreamAttributes(String siddhiAppName, String streamName, List<Attribute> attributes) {
        if (streamAttributesMap.containsKey(siddhiAppName)) {
            streamAttributesMap.get(siddhiAppName).put(streamName, attributes);
        } else {
            streamAttributesMap.put(siddhiAppName, new HashMap<String, List<Attribute>>() {
                {
                    put(streamName, attributes);
                }
            });
        }
    }
}
