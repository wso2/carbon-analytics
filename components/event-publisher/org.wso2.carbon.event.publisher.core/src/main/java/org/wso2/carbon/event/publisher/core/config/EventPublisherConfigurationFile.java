/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.publisher.core.config;

import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * To create objects of event adapter file details
 */
public class EventPublisherConfigurationFile {

    public enum Status {DEPLOYED, WAITING_FOR_DEPENDENCY, WAITING_FOR_STREAM_DEPENDENCY, ERROR}

    private String fileName;
    private String eventPublisherName;
    private String filePath;

    //Deployed, Waiting for Dependency, Error
    private Status status;

    private String dependency;

    private String deploymentStatusMessage = "";

    private int tenantId = MultitenantConstants.INVALID_TENANT_ID;

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getDeploymentStatusMessage() {
        return deploymentStatusMessage;
    }

    public void setDeploymentStatusMessage(String deploymentStatusMessage) {
        this.deploymentStatusMessage = deploymentStatusMessage;
    }

    public String getEventPublisherName() {
        return eventPublisherName;
    }

    public void setEventPublisherName(String eventPublisherName) {
        this.eventPublisherName = eventPublisherName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
}
