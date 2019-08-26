/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.parser.service.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Messaging System Configuration.
 */
public class MessagingConfig {

    @SerializedName("clusterId")
    private String clusterId;

    @SerializedName("bootstrapServers")
    private String[] bootstrapServers;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getBootstrapServerURLs() {
        if (bootstrapServers != null && bootstrapServers.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String urls : bootstrapServers) {
                stringBuilder.append(urls.replace("'", "\\'")).append(",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    public MessagingConfig(String clusterId, String[] bootstrapServers) {
        this.clusterId = clusterId;
        this.bootstrapServers = Arrays.copyOf(bootstrapServers, bootstrapServers.length);
    }

    public boolean isEmpty() {
        return clusterId == null || clusterId.isEmpty() || bootstrapServers == null || bootstrapServers.length == 0
                || bootstrapServers[0].isEmpty();
    }
}
