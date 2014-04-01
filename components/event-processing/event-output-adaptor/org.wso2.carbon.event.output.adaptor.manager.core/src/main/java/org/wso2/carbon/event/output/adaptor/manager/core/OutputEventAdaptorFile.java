package org.wso2.carbon.event.output.adaptor.manager.core;/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.axis2.engine.AxisConfiguration;

/**
 * To create objects of event adaptor file details
 */
public class OutputEventAdaptorFile {

    public enum Status {DEPLOYED, WAITING_FOR_DEPENDENCY, ERROR}

    private String fileName;

    //Deployed, Waiting for Dependency, Error
    private Status status;

    private String dependency;

    //private boolean success;

    private String eventAdaptorName;

    private String deploymentStatusMessage="";

    private AxisConfiguration axisConfiguration;


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

    public String isDeploymentStatusMessage() {
        return deploymentStatusMessage;
    }

    public void setDeploymentStatusMessage(String deploymentStatusMessage) {
        this.deploymentStatusMessage = deploymentStatusMessage;
    }

    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }

    public String getEventAdaptorName() {
        return eventAdaptorName;
    }

    public void setEventAdaptorName(String eventAdaptorName) {
        this.eventAdaptorName = eventAdaptorName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
