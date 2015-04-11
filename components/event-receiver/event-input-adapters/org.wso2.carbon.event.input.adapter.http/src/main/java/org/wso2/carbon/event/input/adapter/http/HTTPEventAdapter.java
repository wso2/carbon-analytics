/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.input.adapter.http;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.http.internal.HTTPEventAdapterManager;
import org.wso2.carbon.event.input.adapter.http.internal.util.HTTPEventAdapterConstants;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class HTTPEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();

    public static ExecutorService executorService;
    public HTTPEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;

        //ThreadPoolExecutor will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueueSize = Integer.parseInt(globalProperties.get(
                        HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueueSize = HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(jobQueueSize));
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        HTTPEventAdapterManager.registerDynamicEndpoint(eventAdapterConfiguration.getName(), this);
    }

    @Override
    public void disconnect() {
        HTTPEventAdapterManager.unregisterDynamicEndpoint(eventAdapterConfiguration.getName(), this);
    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdaptorListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HTTPEventAdapter)) return false;

        HTTPEventAdapter that = (HTTPEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}