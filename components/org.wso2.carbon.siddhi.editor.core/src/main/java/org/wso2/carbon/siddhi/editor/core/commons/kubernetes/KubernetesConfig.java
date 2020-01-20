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

/**
 * Kubernetes configurations of the export apps request.
 */
public class KubernetesConfig {
    private MessagingSystem messagingSystem;
    private PersistentVolumeClaim persistentVolumeClaim;
    private String siddhiProcessName;

    public KubernetesConfig(
            MessagingSystem messagingSystem,
            PersistentVolumeClaim persistentVolumeClaim,
            String siddhiProcessName
    ) {
        this.messagingSystem = messagingSystem;
        this.persistentVolumeClaim = persistentVolumeClaim;
        this.siddhiProcessName = siddhiProcessName;
    }

    public KubernetesConfig(){}

    public MessagingSystem getMessagingSystem() {
        return messagingSystem;
    }

    public PersistentVolumeClaim getPersistentVolumeClaim() {
        return persistentVolumeClaim;
    }

    public String getSiddhiProcessName() {
        return siddhiProcessName;
    }

    public void setMessagingSystem(MessagingSystem messagingSystem) {
        this.messagingSystem = messagingSystem;
    }

    public void setPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        this.persistentVolumeClaim = persistentVolumeClaim;
    }

    public void setSiddhiProcessName(String siddhiProcessName) {
        this.siddhiProcessName = siddhiProcessName;
    }
}
