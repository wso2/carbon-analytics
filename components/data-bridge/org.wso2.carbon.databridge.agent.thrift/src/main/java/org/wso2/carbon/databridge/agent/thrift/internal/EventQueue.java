/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.agent.thrift.internal;

import org.wso2.carbon.databridge.commons.Event;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queue containing the incoming events of a DataPublisher.
 *
 * @param <E>
 */
public class EventQueue<E> {
    private LinkedBlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    private volatile boolean isScheduledForEventDispatching = false;

    /**
     * Polls the event queue and updates the dispatching state of the queue
     *
     * @return event if exist
     */
    public synchronized Event poll() {
        Event event = eventQueue.poll();
        if (null == event) {
            //no more events ot dispatch
            isScheduledForEventDispatching = false;
        }
        return event;
    }

    /**
     * Puts the event to the Queue and informs the dispatching state of the queue
     *
     * @param event to be sent
     * @return true if event queue is scheduled to be dispatches else false
     * @throws InterruptedException
     */
    public synchronized boolean put(Event event) throws InterruptedException {
        eventQueue.put(event);
        if (!isScheduledForEventDispatching) {
            isScheduledForEventDispatching = true;
            return false;
        }
        return isScheduledForEventDispatching;
    }

    public synchronized short tryPut(Event event) throws InterruptedException {
        // Return 0 if isScheduledForEventDispatching is false
        // Return 1 if isScheduledForEventDispatching is true
        // Return 2 if eventQueue is full
        if(!eventQueue.offer(event)){
            return 2;
        }
        if (!isScheduledForEventDispatching) {
            isScheduledForEventDispatching = true;
            return 0;
        }
        return 1;
    }

    public synchronized LinkedBlockingQueue<Event> getAndResetQueue() {
        LinkedBlockingQueue<Event> oldQueue = eventQueue;
        eventQueue = new LinkedBlockingQueue<Event>();
        isScheduledForEventDispatching = false;
        return oldQueue;
    }

}
