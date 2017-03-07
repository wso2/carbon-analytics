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
package org.wso2.carbon.event.publisher.admin;

/**
 * to store Not deployed event publisher configuration file details (filepath & event publisher name)
 */
public class EventPublisherConfigurationFileDto {

    private String fileName;
    private String eventPublisherName;
    private String deploymentStatusMsg;

    public EventPublisherConfigurationFileDto(String fileName, String eventPublisherName, String deploymentStatusMsg) {
        this.fileName = fileName;
        this.eventPublisherName = eventPublisherName;
        this.deploymentStatusMsg = deploymentStatusMsg;
    }

    public String getDeploymentStatusMsg() {
        return deploymentStatusMsg;
    }

    public void setDeploymentStatusMsg(String deploymentStatusMsg) {
        this.deploymentStatusMsg = deploymentStatusMsg;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEventPublisherName() {
        return eventPublisherName;
    }

    public void setEventPublisherName(String eventPublisherName) {
        this.eventPublisherName = eventPublisherName;
    }
}
