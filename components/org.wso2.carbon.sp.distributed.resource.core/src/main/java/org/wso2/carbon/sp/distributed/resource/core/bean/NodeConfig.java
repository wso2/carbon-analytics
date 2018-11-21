/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sp.distributed.resource.core.bean;

import org.wso2.carbon.stream.processor.statistics.bean.WorkerMetrics;

import java.io.Serializable;

/**
 * This class represents the node configuration.
 */
public class NodeConfig implements Serializable {
    private static final long serialVersionUID = 4570286150502045046L;
    /**
     * Id of the node.
     */
    private String id = "wso2-sp";
    /**
     * Advertised HTTPS Host:port configurations of the node.
     */
    private HTTPSInterfaceConfig httpsInterface;
    /**
     * State of the node representing whether the node is NEW or EXISTS.
     * EXISTS means there're deployed artifacts.
     */
    private String state;

    /**
     * Keep the real time metrics details of the resource node.
     */
    private WorkerMetrics workerMetrics;

    /**
     * Specify whether the resource node is type, ReceiverNode
     */
    private boolean isReceiverNode;

    /**
     * Getter for the node id.
     *
     * @return id.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for the node id
     *
     * @param id node id.
     * @return current {@link NodeConfig}
     */
    public NodeConfig setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for the httpsInterface of the node.
     *
     * @return httpsInterface.
     */
    public HTTPSInterfaceConfig getHttpsInterface() {
        return httpsInterface;
    }

    /**
     * Setter for the httpsInterface of the node.
     *
     * @param httpsInterface httpsInterface of the node.
     * @return current {@link NodeConfig}
     */
    public NodeConfig setHttpsInterface(HTTPSInterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
        return this;
    }

    /**
     * Getter for the state of the node.
     *
     * @return httpsInterface.
     */
    public String getState() {
        return state;
    }

    /**
     * Setter for the state of the node.
     *
     * @param state state of the node.
     */
    public NodeConfig setState(String state) {
        this.state = state;
        return this;
    }

    public WorkerMetrics getWorkerMetrics() {
        return workerMetrics;
    }

    public void setWorkerMetrics(WorkerMetrics workerMetrics) {
        this.workerMetrics = workerMetrics;
    }

    public boolean isReceiverNode() {
        return isReceiverNode;
    }

    public NodeConfig setReceiverNode(boolean receiverNode) {
        isReceiverNode = receiverNode;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Node { id: %s, host: %s, port: %s, state: %s }",
                id, httpsInterface.getHost(), httpsInterface.getPort(), state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeConfig that = (NodeConfig) o;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        return getHttpsInterface() != null
                ? getHttpsInterface().equals(that.getHttpsInterface())
                : that.getHttpsInterface() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getHttpsInterface() != null ? getHttpsInterface().hashCode() : 0);
        return result;
    }
}
