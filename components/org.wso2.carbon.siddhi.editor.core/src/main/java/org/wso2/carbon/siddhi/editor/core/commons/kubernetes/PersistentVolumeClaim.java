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

import java.util.List;

/**
 * Configurations of the Kubernetes persistent volume claim.
 */
public class PersistentVolumeClaim {
    private PersistentResources resources;
    private String storageClassName;
    private String volumeMode;
    private List<String> accessModes;

    public PersistentVolumeClaim(){}

    public PersistentResources getResources() {
        return resources;
    }

    public String getStorageClassName() {
        return storageClassName;
    }

    public String getVolumeMode() {
        return volumeMode;
    }

    public List<String> getAccessModes() {
        return accessModes;
    }

    public void setResources(PersistentResources resources) {
        this.resources = resources;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }

    public void setVolumeMode(String volumeMode) {
        this.volumeMode = volumeMode;
    }

    public void setAccessModes(List<String> accessModes) {
        this.accessModes = accessModes;
    }
}
