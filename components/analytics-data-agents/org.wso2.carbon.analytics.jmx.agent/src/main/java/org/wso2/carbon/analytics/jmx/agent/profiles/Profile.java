/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.analytics.jmx.agent.profiles;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Profile {

    private String url;
    private String userName;
    private String pass;
    private String name;
    private boolean active;
    private String cronExpression;
    private int version;

    private MBean[] selectedMBeans;

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPass() {
        return pass;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    @XmlElement
    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @XmlElement
    public void setPass(String pass) {
        this.pass = pass;
    }

    @XmlElement
    public void setActive(boolean active) {
        this.active = active;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    @XmlElement
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public int getVersion() {
        return version;
    }

    @XmlElement
    public void setVersion(int version) {
        this.version = version;
    }

    public MBean[] getSelectedMBeans() {
        return selectedMBeans;
    }

    @XmlElement
    public void setSelectedMBeans(MBean[] selectedMBeans) {
        this.selectedMBeans = selectedMBeans;
    }
}

