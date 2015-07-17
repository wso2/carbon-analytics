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
package org.wso2.carbon.databridge.agent.endpoint;


import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.exception.EventQueueFullException;
import org.wso2.carbon.databridge.agent.util.DataEndpointConstants;
import org.wso2.carbon.databridge.agent.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeThreadFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class holds the endpoints associated within a group. Also it has a queue
 * to hold the list of events that needs to be processed by the endpoints with
 * provided the load balancing, or failover configuration.
 */

public class DataEndpointGroup implements DataEndpointFailureCallback {
    private static final Log log = LogFactory.getLog(DataEndpointGroup.class);

    private List<DataEndpoint> dataEndpoints;

    private HAType haType;

    private EventQueue eventQueue;

    private int reconnectionInterval;

    private AtomicInteger currentDataPublisherIndex = new AtomicInteger();

    private AtomicInteger maximumDataPublisherIndex = new AtomicInteger();

    private ScheduledExecutorService reconnectionService;

    private final Integer START_INDEX = 0;

    public enum HAType {
        FAILOVER, LOADBALANCE
    }

    public DataEndpointGroup(HAType haType, DataEndpointAgent agent) {
        this.dataEndpoints = new ArrayList<>();
        this.haType = haType;
        this.reconnectionService = Executors.newScheduledThreadPool(1, new DataBridgeThreadFactory("ReconnectionService"));
        this.reconnectionInterval = agent.getAgentConfiguration().getReconnectionInterval();
        this.eventQueue = new EventQueue(agent.getAgentConfiguration().getQueueSize());
        this.reconnectionService.scheduleAtFixedRate(new ReconnectionTask(), reconnectionInterval,
                reconnectionInterval, TimeUnit.SECONDS);
    }

    public void addDataEndpoint(DataEndpoint dataEndpoint) {
        dataEndpoints.add(dataEndpoint);
        dataEndpoint.registerDataEndpointFailureCallback(this);
        maximumDataPublisherIndex.incrementAndGet();
    }

    public void tryPublish(Event event) throws EventQueueFullException {
        eventQueue.tryPut(event);
    }

    public void tryPublish(Event event, long timeoutMS) throws EventQueueFullException {
        eventQueue.tryPut(event, timeoutMS);
    }

    public void publish(Event event) {
        eventQueue.put(event);
    }

    class EventQueue {
        private RingBuffer<Event> ringBuffer;
        private Disruptor<Event> eventQueueDisruptor;
        private ExecutorService eventQueuePool;

        public final EventFactory<Event> EVENT_FACTORY = new EventFactory<Event>() {
            public Event newInstance() {
                return new Event();
            }
        };

        EventQueue(int queueSize) {
            eventQueuePool = Executors.newCachedThreadPool(new DataBridgeThreadFactory("EventQueue"));
            eventQueueDisruptor = new Disruptor<>(EVENT_FACTORY, queueSize, eventQueuePool);
            eventQueueDisruptor.handleEventsWith(new EventQueueWorker());
            this.ringBuffer = eventQueueDisruptor.start();
        }

        private void tryPut(Event event) throws EventQueueFullException {
            long sequence;
            try {
                sequence = this.ringBuffer.tryNext(1);
                Event bufferedEvent = this.ringBuffer.get(sequence);
                updateEvent(bufferedEvent, event);
                this.ringBuffer.publish(sequence);
            } catch (InsufficientCapacityException e) {
                throw new EventQueueFullException("Cannot send events because the event queue is full", e);
            }
        }

        private void tryPut(Event event, long timeoutMS) throws EventQueueFullException {
            long sequence;
            long stopTime = System.currentTimeMillis() + timeoutMS;
            while (true) {
                try {
                    sequence = this.ringBuffer.tryNext(1);
                    Event bufferedEvent = this.ringBuffer.get(sequence);
                    updateEvent(bufferedEvent, event);
                    this.ringBuffer.publish(sequence);
                    break;
                } catch (InsufficientCapacityException ex) {
                    if (stopTime > System.currentTimeMillis()) {
                        throw new EventQueueFullException("Cannot send events because the event queue is full", ex);
                    }
                }
            }
        }

        //Endless wait if at-least once endpoint is available.
        private void put(Event event) {
            do {
                try {
                    long sequence = this.ringBuffer.tryNext(1);
                    Event bufferedEvent = this.ringBuffer.get(sequence);
                    updateEvent(bufferedEvent, event);
                    this.ringBuffer.publish(sequence);
                    return;
                } catch (InsufficientCapacityException ignored) {
                }
            } while (isActiveDataEndpointExists());
        }

        private void updateEvent(Event oldEvent, Event newEvent) {
            oldEvent.setArbitraryDataMap(newEvent.getArbitraryDataMap());
            oldEvent.setCorrelationData(newEvent.getCorrelationData());
            oldEvent.setMetaData(newEvent.getMetaData());
            oldEvent.setPayloadData(newEvent.getPayloadData());
            oldEvent.setStreamId(newEvent.getStreamId());
            oldEvent.setTimeStamp(newEvent.getTimeStamp());
        }

        private void shutdown() {
            eventQueuePool.shutdown();
            eventQueueDisruptor.shutdown();
        }
    }

    class EventQueueWorker implements EventHandler<Event> {

        @Override
        public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
            DataEndpoint endpoint = getDataEndpoint(true);
            if (endpoint != null) {
                endpoint.collectAndSend(event);
                if (endOfBatch) {
                    flushAllDataEndpoints();
                }
            } else {
                log.error("Dropping event as DataPublisher is shutting down.");
                if (log.isDebugEnabled()) {
                    log.debug("Data publisher is shutting down, dropping event : " + event);
                }
            }
        }
    }

    private void flushAllDataEndpoints() {
        for (DataEndpoint dataEndpoint : dataEndpoints) {
            if (dataEndpoint.getState().equals(DataEndpoint.State.ACTIVE)) {
                dataEndpoint.flushEvents();
            }
        }
    }

    /**
     * Find the next event processable endpoint to the
     * data endpoint based on load balancing and failover logic, and wait
     * indefinitely until at least one data endpoint becomes available based
     * on busywait parameter.
     *
     * @param busyWait waitUntil atleast one endpoint becomes available
     * @return DataEndpoint which can accept and send the events.
     */
    private DataEndpoint getDataEndpoint(boolean busyWait) {
        int startIndex;
        if (haType.equals(HAType.FAILOVER)) {
            startIndex = getDataPublisherIndex();
        } else {
            startIndex = START_INDEX;
        }
        int index = startIndex;

        while (true) {
            DataEndpoint dataEndpoint = dataEndpoints.get(index);
            if (dataEndpoint.getState().equals(DataEndpoint.State.ACTIVE)) {
                return dataEndpoint;
            } else if (dataEndpoint.getState().equals(DataEndpoint.State.BUSY) && haType.equals(HAType.FAILOVER)) {
                /**
                 * Wait for some time until the failover endpoint finish publishing
                 *
                 */
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                    //ignored
                }
            } else {
                index++;
                if (index > maximumDataPublisherIndex.get() - 1) {
                    index = START_INDEX;
                }
                if (index == startIndex) {
                    if (busyWait && !reconnectionService.isShutdown()) {
                        /**
                         * Have fully iterated the data publisher list,
                         * and busy wait until data publisher
                         * becomes available
                         */
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            //Ignored
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    private boolean isActiveDataEndpointExists() {
        int index = START_INDEX;
        while (index < maximumDataPublisherIndex.get()) {
            DataEndpoint dataEndpoint = dataEndpoints.get(index);
            if (dataEndpoint.getState() != DataEndpoint.State.UNAVAILABLE) {
                return true;
            }
        }
        return false;
    }

    private synchronized int getDataPublisherIndex() {
        int index = currentDataPublisherIndex.getAndIncrement();
        if (index == maximumDataPublisherIndex.get()) {
            currentDataPublisherIndex.set(START_INDEX);
        }
        return index;
    }

    public void tryResendEvents(List<Event> events) {
        List<Event> unsuccessfulEvents = trySendActiveEndpoints(events);
        for (Event event : unsuccessfulEvents) {
            try {
                eventQueue.tryPut(event);
            } catch (EventQueueFullException e) {
                log.error("Unable to put the event :" + event, e);
            }
        }
    }

    private List<Event> trySendActiveEndpoints(List<Event> events) {
        ArrayList<Event> unsuccessfulEvents = new ArrayList<>();
        for (Event event : events) {
            DataEndpoint endpoint = getDataEndpoint(false);
            if (endpoint != null) {
                endpoint.collectAndSend(event);
            } else {
                unsuccessfulEvents.add(event);
            }
        }
        flushAllDataEndpoints();
        return unsuccessfulEvents;
    }

    private class ReconnectionTask implements Runnable {
        public void run() {
            boolean isOneReceiverConnected = false;
            for (int i = START_INDEX; i < maximumDataPublisherIndex.get(); i++) {
                DataEndpoint dataEndpoint = dataEndpoints.get(i);
                if (!dataEndpoint.isConnected()) {
                    try {
                        dataEndpoint.connect();
                    } catch (Exception ex) {
                        dataEndpoint.deactivate();
                    }
                } else {
                    String[] urlElements = DataPublisherUtil.getProtocolHostPort(
                            dataEndpoint.getDataEndpointConfiguration().getReceiverURL());
                    if (!isServerExists(urlElements[1], Integer.parseInt(urlElements[2]))) {
                        dataEndpoint.deactivate();
                    }
                }
                if (dataEndpoint.isConnected()) {
                    isOneReceiverConnected = true;
                }
            }
            if (!isOneReceiverConnected) {
                log.info("No receiver is reachable at reconnection, will try to reconnect every " + reconnectionInterval + " sec");
            }
        }

        private boolean isServerExists(String ip, int port) {
            try {
                new Socket(ip, port);
                return true;
            } catch (UnknownHostException e) {
                return false;
            } catch (IOException e) {
                return false;
            } catch (Exception e) {
                return false;
            }
        }

    }

    public String toString() {
        StringBuilder group = new StringBuilder();
        group.append("[ ");
        for (int i = 0; i < dataEndpoints.size(); i++) {
            DataEndpoint endpoint = dataEndpoints.get(i);
            group.append(endpoint.toString());
            if (i == dataEndpoints.size() - 1) {
                group.append(" ]");
                return group.toString();
            } else {
                if (haType == HAType.FAILOVER) {
                    group.append(DataEndpointConstants.FAILOVER_URL_GROUP_SEPARATOR);
                } else {
                    group.append(DataEndpointConstants.LB_URL_GROUP_SEPARATOR);
                }
            }
        }
        return group.toString();
    }

    public void shutdown() {
        reconnectionService.shutdownNow();
        eventQueue.shutdown();
        for (DataEndpoint dataEndpoint : dataEndpoints) {
            dataEndpoint.shutdown();
        }
    }
}
