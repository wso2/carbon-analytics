/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.config;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.engine.AxisConfiguration;

public class EventBuilderConfigurationFile {

    private String eventBuilderName;
    private String streamWithVersion;
    private String fileName;
    private DeploymentStatus deploymentStatus;
    private String dependency;
    private String deploymentStatusMessage="";
    private AxisConfiguration axisConfiguration;
    private OMElement ebConfigOmElement;

    public EventBuilderConfigurationFile(String fileName) {
        this.fileName = fileName;
    }

    public OMElement getEbConfigOmElement() {
        return ebConfigOmElement;
    }

    public void setEbConfigOmElement(OMElement ebConfigOmElement) {
        this.ebConfigOmElement = ebConfigOmElement;
    }

    public String getStreamWithVersion() {
        return streamWithVersion;
    }

    public void setStreamWithVersion(String streamWithVersion) {
        this.streamWithVersion = streamWithVersion;
    }

    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(DeploymentStatus deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
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

    public AxisConfiguration getAxisConfiguration() {
        return axisConfiguration;
    }

    public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
        this.axisConfiguration = axisConfiguration;
    }

    public String getEventBuilderName() {
        return eventBuilderName;
    }

    public void setEventBuilderName(String eventBuilderName) {
        this.eventBuilderName = eventBuilderName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EventBuilderConfigurationFile that = (EventBuilderConfigurationFile) o;

        if (eventBuilderName != null ? !eventBuilderName.equals(that.eventBuilderName) : that.eventBuilderName != null) {
            return false;
        }
        if (streamWithVersion != null ? !streamWithVersion.equals(that.streamWithVersion) : that.streamWithVersion != null) {
            return false;
        }
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventBuilderName.hashCode();
        result = 31 * result + fileName.hashCode();
        result = 31 * result + (streamWithVersion != null ? streamWithVersion.hashCode() : 0);
        return result;
    }

    public static enum DeploymentStatus {
        //Deployed, Waiting for Dependency, Error
        DEPLOYED, WAITING_FOR_DEPENDENCY, ERROR , WAITING_FOR_STREAM_DEPENDENCY
    }
}
