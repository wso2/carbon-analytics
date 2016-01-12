/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.input.adapter.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSubscription;
import org.wso2.carbon.event.input.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2/27/15.
 */
public class InputAdapterRuntime implements InputEventAdapterListener {
    private static Log log = LogFactory.getLog(InputAdapterRuntime.class);
    private InputEventAdapter inputEventAdapter;
    private String name;
    private InputEventAdapterSubscription inputEventAdapterSubscription;
    private volatile boolean connected = false;
    private DecayTimer timer = new DecayTimer();
    private volatile long nextConnectionTime;
    private ExecutorService executorService;
    private boolean startedTriggered = false;
    private boolean startPollingTriggered = false;
    private int tenantId;

    public InputAdapterRuntime(InputEventAdapter inputEventAdapter, String name,
                               InputEventAdapterSubscription inputEventAdapterSubscription) throws InputEventAdapterException {
        this.inputEventAdapter = inputEventAdapter;
        this.name = name;
        this.inputEventAdapterSubscription = inputEventAdapterSubscription;
        this.tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        executorService = Executors.newSingleThreadExecutor();
        synchronized (this) {
            inputEventAdapter.init(this);
        }
    }

    public void startPolling() {
        startPollingTriggered = true;
        if (!connected && startedTriggered && isPolling()) {
            start();
        }
    }

    public void start() {
        try {
            startedTriggered = true;
            if (!isPolling() || startPollingTriggered) {
                if (!connected) {
                    log.info("Connecting receiver " + this.name);
                    inputEventAdapter.connect();
                    connected = true;
                }
            } else {
                log.info("Waiting to connect receiver " + this.name);
            }
        } catch (ConnectionUnavailableException e) {
            connectionUnavailable(e);
        } catch (InputEventAdapterRuntimeException e) {
            connected = false;
            inputEventAdapter.disconnect();
            log.error("Error initializing Input Adapter '" + this.name + ", hence this will be suspended indefinitely, " + e.getMessage(), e);
        }
    }

    public void destroy() {
        if (inputEventAdapter != null) {
            try {
                inputEventAdapter.disconnect();
            } catch (Throwable e) {
                log.error("Error when disconnecting Input Adapter '" + name + "'," +
                        e.getMessage(), e);
            } finally {
                inputEventAdapter.destroy();
            }
        }
    }

    /**
     * when an event happens event proxy call this method with the received event.
     *
     * @param object - received event
     */
    @Override
    public void onEvent(Object object) {
        inputEventAdapterSubscription.onEvent(object);
    }

    @Override
    public synchronized void connectionUnavailable(ConnectionUnavailableException connectionUnavailableException) {
        try {
            try {
                if (!connected && connectionUnavailableException == null) {
                    if (nextConnectionTime <= System.currentTimeMillis()) {
                        inputEventAdapter.connect();
                        connected = true;
                        timer.reset();
                    }
                } else {
                    connected = false;
                    inputEventAdapter.disconnect();
                    timer.incrementPosition();
                    nextConnectionTime = System.currentTimeMillis() + timer.returnTimeToWait();
                    if (timer.returnTimeToWait() == 0) {
                        log.error("Connection unavailable for Input Adapter '" + name + "' reconnecting.", connectionUnavailableException);
                        inputEventAdapter.connect();
                    } else {
                        log.error("Connection unavailable for Input Adapter '" + name + "' . Reconnection will be retried in " + (timer.returnTimeToWait()) + " milliseconds.", connectionUnavailableException);
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    PrivilegedCarbonContext.startTenantFlow();
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                                    try {
                                        Thread.sleep(timer.returnTimeToWait());
                                    } catch (InterruptedException e) {
                                        //nothing to be done
                                    }
                                    connectionUnavailable(null);
                                } finally {
                                    PrivilegedCarbonContext.endTenantFlow();
                                }
                            }
                        });
                    }
                }
            } catch (ConnectionUnavailableException e) {
                connectionUnavailable(e);
            }
        } catch (InputEventAdapterRuntimeException e) {
            connected = false;
            log.error("Error in connecting Input Adapter '" + this.name + "', hence this will be suspended indefinitely, " + e.getMessage(), e);
        }

    }

    public boolean isEventDuplicatedInCluster() {
        return inputEventAdapter.isEventDuplicatedInCluster();
    }

    public boolean isPolling() {
        return inputEventAdapter.isPolling();
    }

    public String getName() {
        return name;
    }
}
