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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * configuration details related to DataReceiver
 */
@XmlRootElement(name = "dataBridgeConfiguration")
public class DataBridgeConfiguration {

    private List<DataReceiver> dataReceivers;
    private int workerThreads;
    private int eventBufferCapacity;
    private int clientTimeoutMin;

    @XmlElement(name = "dataReceiver")
    public List<DataReceiver> getDataReceivers() {
        return dataReceivers;
    }

    public void setDataReceivers(List<DataReceiver> dataReceivers) {
        this.dataReceivers = dataReceivers;
    }

    @XmlElement(name = "workerThreads")
    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    @XmlElement(name = "eventBufferCapacity")
    public int getEventBufferCapacity() {
        return eventBufferCapacity;
    }

    public void setEventBufferCapacity(int eventBufferCapacity) {
        this.eventBufferCapacity = eventBufferCapacity;
    }

    @XmlElement(name = "clientTimeoutMin")
    public int getClientTimeoutMin() {
        return clientTimeoutMin;
    }

    public void setClientTimeoutMin(int clientTimeoutMin) {
        this.clientTimeoutMin = clientTimeoutMin;
    }

    public DataReceiver getDataReceiver(String name){
        for (DataReceiver dataReceiver: dataReceivers){
            if (dataReceiver.getName().equalsIgnoreCase(name)){
                return dataReceiver;
            }
        }
        return null;
    }
}
