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
package org.wso2.carbon.event.input.adapter.file;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.file.internal.listener.FileTailerListener;
import org.wso2.carbon.event.input.adapter.file.internal.util.FileEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.file.internal.util.FileTailerManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdapterListener;
    private final String id = UUID.randomUUID().toString();
    private static final Log log = LogFactory.getLog(FileEventAdapter.class);
    private ConcurrentHashMap<String, ConcurrentHashMap<String, FileTailerManager>> tailerMap =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, FileTailerManager>>();
    private ThreadPoolExecutor threadPoolExecutor;

    public FileEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }


    @Override
    public void init(InputEventAdapterListener eventAdapterListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdapterListener;

        //ThreadPoolExecutor will be assigned  if it is null
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(FileEventAdapterConstants.MIN_THREAD_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(FileEventAdapterConstants.MIN_THREAD_NAME));
            } else {
                minThread = FileEventAdapterConstants.MIN_THREAD;
            }

            if (globalProperties.get(FileEventAdapterConstants.MAX_THREAD_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(FileEventAdapterConstants.MAX_THREAD_NAME));
            } else {
                maxThread = FileEventAdapterConstants.MAX_THREAD;
            }

            if (globalProperties.get(FileEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        FileEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = FileEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            if (globalProperties.get(FileEventAdapterConstants.JOB_QUEUE_SIZE_NAME) != null) {
                jobQueueSize = Integer.parseInt(globalProperties.get(
                        FileEventAdapterConstants.JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueueSize = FileEventAdapterConstants.JOB_QUEUE_SIZE;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(jobQueueSize));
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {

        createFileAdapterListener();
    }

    @Override
    public void disconnect() {

        ConcurrentHashMap<String, FileTailerManager> tailerManagerConcurrentHashMap = tailerMap.get(
                eventAdapterConfiguration.getName());
        FileTailerManager fileTailerManager = null;
        if (tailerManagerConcurrentHashMap != null) {
            fileTailerManager = tailerManagerConcurrentHashMap.get(eventAdapterConfiguration.getProperties()
                    .get(FileEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH));
        }

        if (fileTailerManager != null) {
            fileTailerManager.getListener().removeListener(id);
            if (fileTailerManager.getListener().hasNoSubscriber()) {
                fileTailerManager.getTailer().stop();
                tailerMap.remove(eventAdapterConfiguration.getName());
            }
        }

    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdapterListener() {
        return eventAdapterListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileEventAdapter)) return false;

        FileEventAdapter that = (FileEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void createFileAdapterListener() {
        log.info("New subscriber added for " + eventAdapterConfiguration.getName());

        ConcurrentHashMap<String, FileTailerManager> tailerManagerConcurrentHashMap = tailerMap
                .get(eventAdapterConfiguration.getName());
        if (tailerManagerConcurrentHashMap == null) {
            tailerManagerConcurrentHashMap = new ConcurrentHashMap<String, FileTailerManager>();
            if (null != tailerMap.putIfAbsent(eventAdapterConfiguration.getName(), tailerManagerConcurrentHashMap)) {
                tailerManagerConcurrentHashMap = tailerMap.get(eventAdapterConfiguration.getName());
            }
        }

        String filepath = eventAdapterConfiguration.getProperties().get(
                FileEventAdapterConstants.EVENT_ADAPTER_CONF_FILEPATH);
        FileTailerManager fileTailerManager = tailerManagerConcurrentHashMap.get(filepath);

        if (fileTailerManager == null) {
            FileTailerListener listener = new FileTailerListener(new File(filepath).getName());
            Tailer tailer = new Tailer(new File(filepath), listener);
            fileTailerManager = new FileTailerManager(tailer, listener);
            listener.addListener(id, eventAdapterListener);
            tailerManagerConcurrentHashMap.put(filepath, fileTailerManager);
            threadPoolExecutor.execute(tailer);
        } else {
            fileTailerManager.getListener().addListener(id, eventAdapterListener);
        }
    }


}
