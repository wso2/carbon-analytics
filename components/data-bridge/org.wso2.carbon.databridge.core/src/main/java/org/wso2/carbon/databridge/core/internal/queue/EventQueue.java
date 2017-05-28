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
package org.wso2.carbon.databridge.core.internal.queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.utils.DataBridgeThreadFactory;
import org.wso2.carbon.databridge.core.AgentCallback;
import org.wso2.carbon.databridge.core.RawDataAgentCallback;
import org.wso2.carbon.databridge.core.Utils.EventComposite;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Event Queue class wraps a thread safe queue to,
 * queue and deque events in a scalable manner
 */
public class EventQueue {

    private static final Log log = LogFactory.getLog(EventQueue.class);

    private BlockingQueue<EventComposite> eventQueue;

    private ExecutorService executorService;
    private List<AgentCallback> subscribers;
    private List<RawDataAgentCallback> rawDataSubscribers;

    public EventQueue(List<AgentCallback> subscribers,
                      List<RawDataAgentCallback> rawDataSubscribers,
                      DataBridgeConfiguration dataBridgeConfiguration) {
        this.subscribers = subscribers;
        this.rawDataSubscribers = rawDataSubscribers;
        // Note : Using a fixed worker thread pool and a bounded queue to prevent the server dying if load is too high
        executorService = Executors.newFixedThreadPool(dataBridgeConfiguration.getWorkerThreads(), new DataBridgeThreadFactory("Core"));
        eventQueue = new EventBlockingQueue(dataBridgeConfiguration.getEventBufferSize(),
                                            dataBridgeConfiguration.getMaxEventBufferCapacity());
    }

    public void publish(EventComposite eventComposite) {
        try {
            eventQueue.put(eventComposite);
        } catch (InterruptedException e) {
            String logMessage = "Failure to insert event into queue";
            log.warn(logMessage);
        }
        executorService.submit(new QueueWorker(eventQueue, subscribers, rawDataSubscribers));
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
        super.finalize();
    }
}
