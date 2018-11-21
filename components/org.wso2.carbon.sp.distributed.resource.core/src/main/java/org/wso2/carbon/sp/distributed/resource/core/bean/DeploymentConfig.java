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


import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents the deployment configuration for distributed deployment.
 */
@Configuration(namespace = "deployment.config", description = "Distributed deployment configuration")
public class DeploymentConfig implements Serializable {
    private static final long serialVersionUID = -1333074544985213851L;
    /**
     * Deployment type. (Distributed/HA).
     */
    @Element(description = "deployment type (distributed/ha)", required = true)
    private String type;
    /**
     * Advertised HTTPS Host:Port configuration of the current node.
     */
    @Element(description = "https host:port configurations", required = true)
    private HTTPSInterfaceConfig httpsInterface;
    /**
     * List of {@link HTTPSInterfaceConfig} for the resource managers.
     */
    private List<HTTPSInterfaceConfig> resourceManagers = null;
    /**
     * Interval between two leader node retry attempts.
     */
    private int leaderRetryInterval = 10000;

    /**
     * Is resource node a ReceiverNode
     */
    private boolean isReceiverNode = false;

    /**
     * Getter for the deployment type.
     *
     * @return deployment type.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for the deployment type.
     *
     * @param type deployment type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for the httpsInterface.
     *
     * @return httpsInterface of the current node.
     */
    public HTTPSInterfaceConfig getHttpsInterface() {
        return httpsInterface;
    }

    /**
     * Setter for the httpsInterface.
     *
     * @param httpsInterface of the current node.
     */
    public void setHttpsInterface(HTTPSInterfaceConfig httpsInterface) {
        this.httpsInterface = httpsInterface;
    }

    /**
     * Getter for the resourceManagers.
     *
     * @return list of {@link HTTPSInterfaceConfig}
     */
    public List<HTTPSInterfaceConfig> getResourceManagers() {
        return resourceManagers;
    }

    /**
     * Setter for the resourceManagers.
     *
     * @param resourceManagers List of {@link HTTPSInterfaceConfig}
     */
    public void setResourceManagers(List<HTTPSInterfaceConfig> resourceManagers) {
        this.resourceManagers = resourceManagers;
    }

    /**
     * Getter for the leaderRetryInterval.
     *
     * @return leaderRetryInterval
     */
    public int getLeaderRetryInterval() {
        return leaderRetryInterval;
    }

    /**
     * Setter for the leaderRetryInterval.
     *
     * @param leaderRetryInterval retry interval in milliseconds.
     */
    public void setLeaderRetryInterval(int leaderRetryInterval) {
        this.leaderRetryInterval = leaderRetryInterval;
    }

    public boolean isReceiverNode() {
        return isReceiverNode;
    }

    public void setReceiverNode(boolean receiverNode) {
        isReceiverNode = receiverNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeploymentConfig that = (DeploymentConfig) o;
        if (isReceiverNode != that.isReceiverNode) {
            return false;
        }
        if (getType() != null
                ? !getType().equals(that.getType())
                : that.getType() != null) {
            return false;
        }
        if (getHttpsInterface() != null
                ? !getHttpsInterface().equals(that.getHttpsInterface())
                : that.getHttpsInterface() != null) {
            return false;
        }
        return getResourceManagers() != null
                ? getResourceManagers().equals(that.getResourceManagers())
                : that.getResourceManagers() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getHttpsInterface() != null ? getHttpsInterface().hashCode() : 0);
        result = 31 * result + (getResourceManagers() != null ? getResourceManagers().hashCode() : 0);
        return result;
    }
}
