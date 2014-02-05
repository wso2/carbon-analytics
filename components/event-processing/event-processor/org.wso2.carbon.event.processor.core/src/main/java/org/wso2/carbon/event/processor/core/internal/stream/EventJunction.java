/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.processor.core.internal.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.siddhi.core.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Acts as the pass through point for a given stream. Does not distinguish between input and output streams.
 */
public class EventJunction {

    private static final Log log = LogFactory.getLog(EventJunction.class);

    /*
     latest stream definition.
      */
    private StreamDefinition streamDefinition;

    /*
     holding the producers this junction is subscribed to.
     incoming events can be from both event builders and siddhi runtimes
      */
    private CopyOnWriteArrayList<EventProducer> producers;

    /*
    listeners of this junction.
    output events can be towards both event formatter and siddhi runtime.
     */
    private CopyOnWriteArrayList<EventConsumer> consumers;

    // TODO 's - what happens when stream is changed by only one instance...,
    // todo 's  prioritize effects when some module changes its stream def.


    public EventJunction(StreamDefinition streamDefinition) {
        this.streamDefinition = streamDefinition;
        this.producers = new CopyOnWriteArrayList<EventProducer>();
        this.consumers = new CopyOnWriteArrayList<EventConsumer>();
    }

    public void dispatchEvent(Object[] eventData) {
        for (EventConsumer consumer : consumers) {
            try {
                consumer.consumeEvent(eventData);
            } catch (Exception e) {
                log.error("Error while dispatching events", e);
            }
        }
    }

    public void dispatchEvents(Object[][] events) {
        for (EventConsumer consumer : consumers) {
            try {
                consumer.consumeEvents(events);
            } catch (Exception e) {
                log.error("Error while dispatching events", e);
            }
        }
    }

    public void dispatchEvents(Event[] events) {
        for (EventConsumer consumer : consumers) {
            try {
                consumer.consumeEvents(events);
            } catch (Exception e) {
                log.error("Error while dispatching events", e);
            }
        }
    }


    public void addConsumer(EventConsumer consumer) {
        if (!consumers.contains(consumer)) {
            log.info("Consumer added to the junction. Stream:" + getStreamDefinition().getStreamId());
            consumers.add(consumer);
        } else {
            log.error("Consumer already exist in the junction: " + streamDefinition.getStreamId());
        }
    }

    public boolean removeConsumer(EventConsumer consumer) {
        return consumers.remove(consumer);
    }

    public void addProducer(EventProducer listener) {
        if (!producers.contains(listener)) {
            log.info("Producer added to the junction. Stream:" + getStreamDefinition().getStreamId());
            producers.add(listener);
        } else {
            log.error("Producer already exist in the junction: " + streamDefinition.getStreamId());
        }
    }

    public boolean removeProducer(EventProducer producer) {
        return producers.remove(producer);
    }

    public List<EventProducer> getAllEventProducers() {
        return new ArrayList<EventProducer>(producers);
    }

    public List<EventConsumer> getAllEventConsumers() {
        return new ArrayList<EventConsumer>(consumers);
    }

    public StreamDefinition getStreamDefinition() {
        return streamDefinition;
    }

}
