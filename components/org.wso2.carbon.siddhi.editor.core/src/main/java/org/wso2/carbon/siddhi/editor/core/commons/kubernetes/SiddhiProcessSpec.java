/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.commons.kubernetes;

import java.util.ArrayList;

/**
 * Kubernetes Siddhi process specification.
 */
public class SiddhiProcessSpec {
    private ArrayList<SiddhiProcessApp> apps;
    private SiddhiProcessContainer container;
    private MessagingSystem messagingSystem;
    private PersistentVolumeClaim persistentVolumeClaim;
    private String runner;

    public ArrayList<SiddhiProcessApp> getApps() {
        return apps;
    }

    public SiddhiProcessContainer getContainer() {
        return container;
    }

    public MessagingSystem getMessagingSystem() {
        return messagingSystem;
    }

    public PersistentVolumeClaim getPersistentVolumeClaim() {
        return persistentVolumeClaim;
    }

    public String getRunner() {
        return runner;
    }

    public void setApps(ArrayList<SiddhiProcessApp> apps) {
        this.apps = apps;
    }

    public void setContainer(SiddhiProcessContainer container) {
        this.container = container;
    }

    public void setMessagingSystem(MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    public void setPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        this.persistentVolumeClaim = persistentVolumeClaim;
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }
}
