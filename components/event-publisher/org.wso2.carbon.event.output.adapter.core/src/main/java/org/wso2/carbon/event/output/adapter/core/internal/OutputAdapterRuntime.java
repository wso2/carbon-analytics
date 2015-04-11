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

package org.wso2.carbon.event.output.adapter.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;

import java.util.Map;

/**
 * Created on 2/27/15.
 */
public class OutputAdapterRuntime {
    private static final Log log = LogFactory.getLog(OutputAdapterRuntime.class);
    private final OutputEventAdapter outputEventAdapter;
    private final String name;
    private volatile boolean connected = false;
    private final DecayTimer timer = new DecayTimer();
    private volatile long nextConnectionTime;


    public OutputAdapterRuntime(OutputEventAdapter outputEventAdapter, String name) throws OutputEventAdapterException {
        this.outputEventAdapter = outputEventAdapter;
        this.name = name;
        synchronized (this) {
            outputEventAdapter.init();
            try {
                outputEventAdapter.connect();
                connected = true;
            } catch (ConnectionUnavailableException e) {
                connected = false;
                outputEventAdapter.disconnect();
                log.error("Error initializing connecting on " + this.name + ", reconnection will be tried on next event arrival.", e);
            } catch (OutputEventAdapterRuntimeException e) {
                connected = false;
                outputEventAdapter.disconnect();
                log.error("Error initializing connecting on " + this.name + ", reconnection will be tried on next event arrival.", e);
            }
        }
    }

    public void publish(Object message, Map<String, String> dynamicProperties) {
        try {
            try {
                if (connected) {
                    outputEventAdapter.publish(message, dynamicProperties);
                } else {
                    if (nextConnectionTime <= System.currentTimeMillis()) {
                        synchronized (this) {
                            if (!connected) {
                                if (nextConnectionTime <= System.currentTimeMillis()) {
                                    outputEventAdapter.connect();
                                    outputEventAdapter.publish(message, dynamicProperties);
                                    connected = true;
                                    timer.reset();
                                } else {
                                    logAndDrop();
                                }
                            } else {
                                outputEventAdapter.publish(message, dynamicProperties);
                            }
                        }
                    } else {
                        logAndDrop();
                    }
                }
            } catch (ConnectionUnavailableException e) {
                connected = false;
                if (nextConnectionTime <= System.currentTimeMillis()) {
                    synchronized (this) {
                        if (nextConnectionTime <= System.currentTimeMillis()) {
                            outputEventAdapter.disconnect();
                            timer.incrementPosition();
                            nextConnectionTime = System.currentTimeMillis() + timer.returnTimeToWait();
                            if (timer.returnTimeToWait() == 0) {
                                log.error("Connection unavailable on " + name + " reconnecting.", e);
                                publish(message, dynamicProperties);
                            } else {
                                log.error("Connection unavailable on " + name + " reconnection will be retried in" + (timer.returnTimeToWait()) + " milliseconds.", e);
                            }
                        } else {
                            logAndDrop();
                        }
                    }
                } else {
                    logAndDrop();
                }
            }
        } catch (OutputEventAdapterRuntimeException e) {
            log.error("Error in sending message " + e.getMessage(), e);
        }

    }

    private void logAndDrop() {
        if (log.isDebugEnabled()) {
            log.debug("Output Adapter '" + name + "' suspended hence dropping event. Adapter will be active after " + (nextConnectionTime - System.currentTimeMillis()) + " milliseconds.");
        }
    }

    public void destroy() {
        if (outputEventAdapter != null) {
            try {
                outputEventAdapter.disconnect();
            } finally {
                outputEventAdapter.destroy();
            }
        }
    }
}
