/**
 *
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.core.conf;

import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;

/**
 * configuration details related to DataReceiver
 */
public class DataBridgeConfiguration {
    private int workerThreads = DataBridgeConstants.NO_OF_WORKER_THREADS;
    private int eventBufferCapacity = DataBridgeConstants.EVENT_BUFFER_CAPACITY;
    private int clientTimeOut = DataBridgeConstants.CLIENT_TIMEOUT_MS;

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getEventBufferCapacity() {
        return eventBufferCapacity;
    }

    public void setEventBufferCapacity(int eventBufferCapacity) {
        this.eventBufferCapacity = eventBufferCapacity;
    }

    public int getClientTimeOut() {
        return clientTimeOut;
    }

    public void setClientTimeOut(int clientTimeOut) {
        this.clientTimeOut = clientTimeOut;
    }
}
