/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.stream.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.EventProducer;
import org.wso2.carbon.event.stream.core.EventProducerCallback;
import org.wso2.carbon.event.stream.core.SiddhiEventConsumer;
import org.wso2.carbon.event.stream.core.WSO2EventConsumer;
import org.wso2.carbon.event.stream.core.WSO2EventListConsumer;
import org.wso2.carbon.event.stream.core.internal.util.EventConverter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Acts as the pass through point for a given stream. Does not distinguish between input and output streams.
 */
public class EventJunction implements EventProducerCallback {

    private static final Log log = LogFactory.getLog(EventJunction.class);

    /*
     latest stream definition.
      */
    private StreamDefinition streamDefinition;

    private boolean metaFlag = false;
    private boolean correlationFlag = false;
    private boolean payloadFlag = false;
    private int attributesCount;


    /*
     holding the producers this junction is subscribed to.
     incoming events can be from both event builders and siddhi runtimes
      */
    private CopyOnWriteArrayList<EventProducer> producers;

    /*
    listeners of this junction.
    output events can be towards both event formatter and siddhi runtime.
     */
    private CopyOnWriteArrayList<SiddhiEventConsumer> siddhiEventConsumers;
    private CopyOnWriteArrayList<WSO2EventConsumer> wso2EventConsumers;
    private CopyOnWriteArrayList<WSO2EventListConsumer> wso2EventListConsumers;

    public EventJunction(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
        this.producers = new CopyOnWriteArrayList<EventProducer>();
        this.siddhiEventConsumers = new CopyOnWriteArrayList<SiddhiEventConsumer>();
        this.wso2EventConsumers = new CopyOnWriteArrayList<WSO2EventConsumer>();
        this.wso2EventListConsumers = new CopyOnWriteArrayList<WSO2EventListConsumer>();
        populateEventTemplate(streamDefinition);
    }

    public void addConsumer(SiddhiEventConsumer consumer) {
        if (!siddhiEventConsumers.contains(consumer)) {
            log.info("Consumer added to the junction. Stream:" + getStreamDefinition().getStreamId());
            siddhiEventConsumers.add(consumer);
        } else {
            log.error("Consumer already exist in the junction: " + streamDefinition.getStreamId());
        }
    }

    public boolean removeConsumer(SiddhiEventConsumer consumer) {
        return siddhiEventConsumers.remove(consumer);
    }

    public void addConsumer(WSO2EventConsumer consumer) {
        if (!wso2EventConsumers.contains(consumer)) {
            log.info("WSO2EventConsumer added to the junction. Stream:" + getStreamDefinition().getStreamId());
            consumer.onAddDefinition(streamDefinition);
            wso2EventConsumers.add(consumer);
        } else {
            log.error("WSO2EventConsumer already exist in the junction: " + streamDefinition.getStreamId());
        }
    }

    public void addConsumer(WSO2EventListConsumer consumer) {
        if (!wso2EventListConsumers.contains(consumer)) {
            log.info("WSO2EventConsumer added to the junction. Stream:" + getStreamDefinition().getStreamId());
            consumer.onAddDefinition(streamDefinition);
            wso2EventListConsumers.add(consumer);
        } else {
            log.error("WSO2EventConsumer already exist in the junction: " + streamDefinition.getStreamId());
        }
    }

    public boolean removeConsumer(WSO2EventConsumer consumer) {
        boolean isRemoved = wso2EventConsumers.remove(consumer);
        consumer.onRemoveDefinition(streamDefinition);
        return isRemoved;
    }

    public boolean removeConsumer(WSO2EventListConsumer consumer) {
        boolean isRemoved = wso2EventListConsumers.remove(consumer);
        consumer.onRemoveDefinition(streamDefinition);
        return isRemoved;
    }

    public void addProducer(EventProducer listener) {
        if (!producers.contains(listener)) {
            log.info("Producer added to the junction. Stream:" + getStreamDefinition().getStreamId());
            listener.setCallBack(this);
            producers.add(listener);
        } else {
            log.error("Producer already exist in the junction: " + streamDefinition.getStreamId());
        }
    }

    public boolean removeProducer(EventProducer producer) {
        boolean isRemoved = producers.remove(producer);
        if (isRemoved) {
            producer.setCallBack(null);
        }
        return isRemoved;
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

    @Override
    public void sendEvent(Event event) {

        if (!siddhiEventConsumers.isEmpty()) {
            org.wso2.siddhi.core.event.Event convertedEvent = EventConverter.convertToEvent(event, metaFlag, correlationFlag, payloadFlag, attributesCount);
            for (SiddhiEventConsumer consumer : siddhiEventConsumers) {
                try {
                    consumer.consumeEvent(convertedEvent);
                } catch (Exception e) {
                    log.error("Error while dispatching events: " + e.getMessage(), e);
                }
            }
        }


        if (!wso2EventConsumers.isEmpty()) {
            for (WSO2EventConsumer consumer : wso2EventConsumers) {
                try {
                    consumer.onEvent(event);
                } catch (Exception e) {
                    log.error("Error while dispatching events: " + e.getMessage(), e);
                }
            }
        }

        if (!wso2EventListConsumers.isEmpty()) {
            for (WSO2EventListConsumer consumer : wso2EventListConsumers) {
                try {
                    consumer.onEvent(event);
                } catch (Exception e) {
                    log.error("Error while dispatching events: " + e.getMessage(), e);
                }
            }
        }

    }

    @Override
    public void sendEvents(List<Event> events) {
        for (Event event : events) {
            if (!siddhiEventConsumers.isEmpty()) {
                org.wso2.siddhi.core.event.Event convertedEvent = EventConverter.convertToEvent(event, metaFlag, correlationFlag, payloadFlag, attributesCount);
                for (SiddhiEventConsumer consumer : siddhiEventConsumers) {
                    try {
                        consumer.consumeEvent(convertedEvent);
                    } catch (Exception e) {
                        log.error("Error while dispatching events: " + e.getMessage(), e);
                    }
                }
            }


            if (!wso2EventConsumers.isEmpty()) {
                for (WSO2EventConsumer consumer : wso2EventConsumers) {
                    try {
                        consumer.onEvent(event);
                    } catch (Exception e) {
                        log.error("Error while dispatching events: " + e.getMessage(), e);
                    }
                }
            }
        }


        if (!wso2EventListConsumers.isEmpty()) {
            for (WSO2EventListConsumer eventListConsumer : wso2EventListConsumers) {
                try {
                    eventListConsumer.onEventList(events);
                } catch (Exception e) {
                    log.error("Error while dispatching events: " + e.getMessage(), e);
                }
            }
        }
    }

    private void populateEventTemplate(StreamDefinition definition) {
        int attributesCount = 0;
        if (definition.getMetaData() != null) {
            attributesCount += definition.getMetaData().size();
            metaFlag = true;
        }
        if (definition.getCorrelationData() != null) {
            attributesCount += definition.getCorrelationData().size();
            correlationFlag = true;
        }

        if (definition.getPayloadData() != null) {
            attributesCount += definition.getPayloadData().size();
            payloadFlag = true;
        }

        this.attributesCount = attributesCount;
    }
}
