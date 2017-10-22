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
     * Advertised Host:port configurations of the node.
     */
    private HTTPInterfaceConfig httpInterface;

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
     * Getter for the httpInterface of the node.
     *
     * @return httpInterface.
     */
    public HTTPInterfaceConfig getHttpInterface() {
        return httpInterface;
    }

    /**
     * Setter for the httpInterface of the node.
     *
     * @param httpInterface httpInterface of the node.
     * @return current {@link NodeConfig}
     */
    public NodeConfig setHttpInterface(HTTPInterfaceConfig httpInterface) {
        this.httpInterface = httpInterface;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Node { id: %s, host: %s, port: %s }",
                id, httpInterface.getHost(), httpInterface.getPort());
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
        return getHttpInterface() != null
                ? getHttpInterface().equals(that.getHttpInterface())
                : that.getHttpInterface() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getHttpInterface() != null ? getHttpInterface().hashCode() : 0);
        return result;
    }
}
