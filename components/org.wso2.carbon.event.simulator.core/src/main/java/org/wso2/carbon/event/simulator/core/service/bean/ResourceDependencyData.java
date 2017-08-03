/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.service.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;

/**
 * ResourceDependencyData holds details about the resource required by the inactive simulation
 */
public class ResourceDependencyData {

    private ResourceNotFoundException.ResourceType resourceType;
    private String resourceName;

    public ResourceDependencyData(ResourceNotFoundException.ResourceType resourceType, String resourceName) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public ResourceNotFoundException.ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceNotFoundException.ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ResourceDependencyData)) {
            return false;
        } else {
            ResourceDependencyData resourceData = (ResourceDependencyData) obj;
            return new EqualsBuilder()
                    .append(resourceType, resourceData.resourceType)
                    .append(resourceName, resourceData.resourceName)
                    .isEquals();
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(resourceType)
                .append(resourceName)
                .toHashCode();
    }

}
