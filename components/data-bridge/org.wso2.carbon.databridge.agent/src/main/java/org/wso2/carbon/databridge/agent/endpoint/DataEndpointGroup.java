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


import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.DataEndpointAgent;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

    private EventQueue eventQueue = null;

    private int reconnectionInterval;

    private final Integer START_INDEX = 0;

    private AtomicInteger currentDataPublisherIndex = new AtomicInteger(START_INDEX);

    private AtomicInteger maximumDataPublisherIndex = new AtomicInteger();

    private ScheduledExecutorService reconnectionService;

    private final String publishingStrategy;

    private boolean isShutdown = false;

    public enum HAType {
        FAILOVER, LOADBALANCE
    }

    public DataEndpointGroup(HAType haType, DataEndpointAgent agent) {
        this.dataEndpoints = new ArrayList<>();
        this.haType = haType;
        this.reconnectionService = Executors.newScheduledThreadPool(1, new DataBridgeThreadFactory("ReconnectionService"));
        this.reconnectionInterval = agent.getAgentConfiguration().getReconnectionInterval();
        this.publishingStrategy = agent.getAgentConfiguration().getPublishingStrategy();
        if (!publishingStrategy.equalsIgnoreCase(DataEndpointConstants.SYNC_STRATEGY)) {
            this.eventQueue = new EventQueue(agent.getAgentConfiguration().getQueueSize());
        }
        this.reconnectionService.scheduleAtFixedRate(new ReconnectionTask(), reconnectionInterval,
                reconnectionInterval, TimeUnit.SECONDS);
        currentDataPublisherIndex.set(START_INDEX);
    }

    public void addDataEndpoint(DataEndpoint dataEndpoint) {
        dataEndpoints.add(dataEndpoint);
        dataEndpoint.registerDataEndpointFailureCallback(this);
        maximumDataPublisherIndex.incrementAndGet();
    }

    public void tryPublish(Event event) throws EventQueueFullException {
        if (eventQueue != null) {
            eventQueue.tryPut(event);
        } else if (!isShutdown) {
            trySyncPublish(event);
        }
    }

    public void tryPublish(Event event, long timeoutMS) throws EventQueueFullException {
        if (eventQueue != null) {
            eventQueue.tryPut(event, timeoutMS);
        } else if (!isShutdown) {
            trySyncPublish(event, timeoutMS);
        }
    }

    public void publish(Event event) {
        if (eventQueue != null) {
            eventQueue.put(event);
        } else if (!isShutdown) {
            syncPublish(event);
        }
    }

    private void trySyncPublish(Event event) {
        try {
            DataEndpoint endpoint = getDataEndpoint(false);
            if (endpoint != null) {
                endpoint.syncSend(event);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("DataEndpoint not available, dropping event : " + event);
                }
            }
        } catch (Throwable t) {
            log.error("Unexpected error: " + t.getMessage(), t);
        }
    }

    private void trySyncPublish(Event event, long timeoutMS) {
        long stopTime = System.currentTimeMillis() + timeoutMS;
        while (true) {
            DataEndpoint endpoint = getDataEndpoint(false);
            if (endpoint != null) {
                endpoint.syncSend(event);
                break;
            }
            if (stopTime <= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("DataEndpoint not available for  last " + timeoutMS + " ms, dropping event : " + event);
                }
                break;
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void syncPublish(Event event) {
        try {
            DataEndpoint endpoint = getDataEndpoint(true);
            if (endpoint != null) {
                endpoint.syncSend(event);
            } else {
                log.error("Dropping event as DataPublisher is shutting down.");
                if (log.isDebugEnabled()) {
                    log.debug("Data publisher is shutting down, dropping event : " + event);
                }
            }
        } catch (Throwable t) {
            log.error("Unexpected error: " + t.getMessage(), t);
        }
    }

    class EventQueue {
        private RingBuffer<WrappedEventFactory.WrappedEvent> ringBuffer = null;
        private Disruptor<WrappedEventFactory.WrappedEvent> eventQueueDisruptor = null;
        private ExecutorService eventQueuePool = null;

        EventQueue(int queueSize) {
            eventQueuePool = Executors.newCachedThreadPool(new DataBridgeThreadFactory("EventQueue"));
            eventQueueDisruptor = new Disruptor<>(new WrappedEventFactory(), queueSize, eventQueuePool, ProducerType.MULTI, new BlockingWaitStrategy());
            eventQueueDisruptor.handleEventsWith(new EventQueueWorker());
            this.ringBuffer = eventQueueDisruptor.start();
        }

        private void tryPut(Event event) throws EventQueueFullException {

            long sequence;
            try {
                sequence = this.ringBuffer.tryNext(1);
                WrappedEventFactory.WrappedEvent bufferedEvent = this.ringBuffer.get(sequence);
                bufferedEvent.setEvent(event);
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
                    WrappedEventFactory.WrappedEvent bufferedEvent = this.ringBuffer.get(sequence);
                    bufferedEvent.setEvent(event);
                    this.ringBuffer.publish(sequence);
                    break;
                } catch (InsufficientCapacityException ex) {
                    if (stopTime <= System.currentTimeMillis()) {
                        throw new EventQueueFullException("Cannot send events because the event queue is full", ex);
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }

        //Endless wait if at-least once endpoint is available.
        private void put(Event event) {
            do {
                try {
                    long sequence = this.ringBuffer.tryNext(1);
                    WrappedEventFactory.WrappedEvent bufferedEvent = this.ringBuffer.get(sequence);
                    bufferedEvent.setEvent(event);
                    this.ringBuffer.publish(sequence);
                    return;
                } catch (InsufficientCapacityException ex) {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException ignored) {
                    }
                }
            } while (isActiveDataEndpointExists());
        }

        private void shutdown() {
            eventQueuePool.shutdown();
            eventQueueDisruptor.shutdown();
        }
    }

    class EventQueueWorker implements EventHandler<WrappedEventFactory.WrappedEvent> {

        boolean isLastEventDropped =false;
        @Override
        public void onEvent(WrappedEventFactory.WrappedEvent wrappedEvent, long sequence, boolean endOfBatch) {
            DataEndpoint endpoint = getDataEndpoint(true);
            Event event = wrappedEvent.getEvent();
            if (endpoint != null) {
                isLastEventDropped =false;
                endpoint.collectAndSend(event);
                if (endOfBatch) {
                    flushAllDataEndpoints();
                }
            } else {
                if(!isLastEventDropped) {
                    log.error("Dropping all events as DataPublisher is shutting down.");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Data publisher is shutting down, dropping event : " + event);
                }
                isLastEventDropped =true;
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
     * @param isBusyWait waitUntil atleast one endpoint becomes available
     * @return DataEndpoint which can accept and send the events.
     */
    private DataEndpoint getDataEndpoint(boolean isBusyWait) {
        int startIndex;
        if (haType.equals(HAType.LOADBALANCE)) {
            startIndex = getDataPublisherIndex();
        } else {
            startIndex = START_INDEX;
        }
        int index = startIndex;

        while (true) {
            DataEndpoint dataEndpoint = dataEndpoints.get(index);
            if (dataEndpoint.getState().equals(DataEndpoint.State.ACTIVE)) {
                return dataEndpoint;
            } else if (haType.equals(HAType.FAILOVER) && (dataEndpoint.getState().equals(DataEndpoint.State.BUSY) ||
                    dataEndpoint.getState().equals(DataEndpoint.State.INITIALIZING))) {
                /**
                 * Wait for some time until the failover endpoint finish publishing
                 *
                 */
                busyWait(1);
            } else {
                index++;
                if (index > maximumDataPublisherIndex.get() - 1) {
                    index = START_INDEX;
                }
                if (index == startIndex) {
                    if (isBusyWait) {
                        if (!reconnectionService.isShutdown()) {

                            /**
                             * Have fully iterated the data publisher list,
                             * and busy wait until data publisher
                             * becomes available
                             */
                            busyWait(1);
                        } else {
                            if (!isActiveDataEndpointExists()) {
                                return null;
                            } else {
                                busyWait(1);
                            }
                        }
                    } else {
                        return null;
                    }
                }
            }
        }
    }

    private void busyWait(long timeInMilliSec) {
        try {
            Thread.sleep(timeInMilliSec);
        } catch (InterruptedException ignored) {
        }
    }

    private boolean isActiveDataEndpointExists() {
        int index = START_INDEX;
        while (index < maximumDataPublisherIndex.get()) {
            DataEndpoint dataEndpoint = dataEndpoints.get(index);
            if (dataEndpoint.getState() != DataEndpoint.State.UNAVAILABLE) {
                if (log.isDebugEnabled()) {
                    log.debug("Available endpoint : " + dataEndpoint + " existing in state - " + dataEndpoint.getState());
                }
                return true;
            }
            index++;
        }
        return false;
    }

    private synchronized int getDataPublisherIndex() {
        int index = currentDataPublisherIndex.getAndIncrement();
        if (index == maximumDataPublisherIndex.get() - 1) {
            currentDataPublisherIndex.set(START_INDEX);
        }
        return index;
    }

    public void tryResendEvents(List<Event> events) {
        List<Event> unsuccessfulEvents = trySendActiveEndpoints(events);
        for (Event event : unsuccessfulEvents) {
            try {
                if (eventQueue != null) {
                    eventQueue.tryPut(event);
                } else {
                    trySyncPublish(event);
                }
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
                    try {
                        String[] urlElements = DataPublisherUtil.getProtocolHostPort(
                                dataEndpoint.getDataEndpointConfiguration().getReceiverURL());
                        if (!isServerExists(urlElements[1], Integer.parseInt(urlElements[2]))) {
                            dataEndpoint.deactivate();
                        }
                    } catch (DataEndpointConfigurationException exception) {
                        log.warn("Data Endpoint with receiver URL:" + dataEndpoint.getDataEndpointConfiguration().getReceiverURL()
                                + " could not be deactivated", exception);
                    }
                }
                if (dataEndpoint.isConnected()) {
                    isOneReceiverConnected = true;
                }
            }
            if (!isOneReceiverConnected) {
                log.warn("No receiver is reachable at reconnection, will try to reconnect every " + reconnectionInterval + " sec");
            }
        }

        private boolean isServerExists(String ip, int port) {
            try {
                Socket socket = new Socket(ip, port);
                socket.close();
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
        if (eventQueue != null) {
            eventQueue.shutdown();
        }
        isShutdown = true;
        for (DataEndpoint dataEndpoint : dataEndpoints) {
            dataEndpoint.shutdown();
        }
    }
}
