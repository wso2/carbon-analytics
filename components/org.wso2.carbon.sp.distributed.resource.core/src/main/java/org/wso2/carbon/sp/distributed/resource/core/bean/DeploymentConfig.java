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
     * Advertised Host:Port configuration of the current node.
     */
    @Element(description = "host:port configurations", required = true)
    private HTTPInterfaceConfig httpInterface;
    /**
     * List of {@link HTTPInterfaceConfig} for the resource managers.
     */
    private List<HTTPInterfaceConfig> resourceManagers = null;

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
     * Getter for the httpInterface.
     *
     * @return httpInterface of the current node.
     */
    public HTTPInterfaceConfig getHttpInterface() {
        return httpInterface;
    }

    /**
     * Setter for the httpInterface.
     *
     * @param httpInterface of the current node.
     */
    public void setHttpInterface(HTTPInterfaceConfig httpInterface) {
        this.httpInterface = httpInterface;
    }

    /**
     * Getter for the resourceManagers.
     *
     * @return list of {@link HTTPInterfaceConfig}
     */
    public List<HTTPInterfaceConfig> getResourceManagers() {
        return resourceManagers;
    }

    /**
     * Setter for the resourceManagers.
     *
     * @param resourceManagers List of {@link HTTPInterfaceConfig}
     */
    public void setResourceManagers(List<HTTPInterfaceConfig> resourceManagers) {
        this.resourceManagers = resourceManagers;
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
        if (getType() != null
                ? !getType().equals(that.getType())
                : that.getType() != null) {
            return false;
        }
        if (getHttpInterface() != null
                ? !getHttpInterface().equals(that.getHttpInterface())
                : that.getHttpInterface() != null) {
            return false;
        }
        return getResourceManagers() != null
                ? getResourceManagers().equals(that.getResourceManagers())
                : that.getResourceManagers() == null;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getHttpInterface() != null ? getHttpInterface().hashCode() : 0);
        result = 31 * result + (getResourceManagers() != null ? getResourceManagers().hashCode() : 0);
        return result;
    }
}
