/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.processor.manager.core.internal;

import com.hazelcast.core.HazelcastInstance;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.processor.manager.core.*;
import org.wso2.carbon.event.processor.manager.core.config.HAConfiguration;
import org.wso2.carbon.event.processor.manager.core.config.ManagementConfigurationException;
import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.carbon.event.processor.manager.core.config.Mode;
import org.wso2.carbon.event.processor.manager.core.internal.util.Constants;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CarbonEventManagementService implements EventManagementService {

    private static Logger log = Logger.getLogger(CarbonEventManagementService.class);

    public ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Mode mode = Mode.SingleNode;
    private ManagementModeInfo managementModeInfo;
    private HAManager haManager = null;
    private ScheduledExecutorService executorService;

    private EventProcessorManagementService processorManager;
    private EventReceiverManagementService receiverManager;
    private EventPublisherManagementService publisherManager;


    public CarbonEventManagementService() {
        try {
            managementModeInfo = ManagementModeInfo.getInstance();
            mode = managementModeInfo.getMode();
        } catch (ManagementConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void init(HazelcastInstance hazelcastInstance) {
        if (mode == Mode.HA) {
            HAConfiguration haConfiguration = managementModeInfo.getHaConfiguration();
            haManager = new HAManager(hazelcastInstance, haConfiguration, readWriteLock.writeLock(), this);
        } else if (mode == Mode.SingleNode) {
            log.warn("CEP started with clustering enabled, but SingleNode configuration given.");
        } else {
            log.warn("CEP started with HA enabled, but distributed configuration given.");
        }

        if (haManager != null) {
            haManager.init();
        }
    }

    public void init(ConfigurationContextService configurationContextService) {
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                log.info("Starting polling event adapters");
                getEventReceiverManagementService().startPolling();
            }
        }, Constants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (haManager != null) {
            haManager.shutdown();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public byte[] getState() {
        if (mode == Mode.HA) {
            return haManager.getState();
        }
        return null;
    }


    public void subscribe(Manager manager){
        if (manager.getType() == Manager.ManagerType.Processor) {
            this.processorManager = (EventProcessorManagementService) manager;
        } else if (manager.getType() == Manager.ManagerType.Receiver) {
            this.receiverManager = (EventReceiverManagementService) manager;
        } else if (manager.getType() == Manager.ManagerType.Publisher) {
            this.publisherManager = (EventPublisherManagementService) manager;
        }
    }

    public EventProcessorManagementService getEventProcessorManagementService() {
        return processorManager;
    }

    public EventReceiverManagementService getEventReceiverManagementService() {
        return receiverManager;
    }

    public EventPublisherManagementService getEventPublisherManagementService() {
        return publisherManager;
    }
}
