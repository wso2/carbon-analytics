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

package io.siddhi.distribution.editor.core.commons.kubernetes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Messaging System Configuration of the Kubernetes deployment.
 */
public class MessagingConfig {

    @SerializedName("streamingClusterId")
    private String streamingClusterId;

    @SerializedName("bootstrapServers")
    private List<String> bootstrapServers;

    public MessagingConfig(String clusterId, List<String> bootstrapServers) {
        this.streamingClusterId = clusterId;
        this.bootstrapServers = bootstrapServers;
    }

    public MessagingConfig(){}

    public String getStreamingClusterId() {
        return streamingClusterId;
    }

    public List<String> getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(List<String> bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void setStreamingClusterId(String streamingClusterId) {
        this.streamingClusterId = streamingClusterId;
    }
}
