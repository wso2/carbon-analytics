/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.stream.processor.statistics.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.stream.processor.core.NodeInfo;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;

/**
 * This is the data holder of stream processor statistics component.
 */
public class StreamProcessorStatisticDataHolder {
    private static final Logger logger = LoggerFactory.getLogger(StreamProcessorStatisticDataHolder.class);
    private static StreamProcessorStatisticDataHolder instance = new StreamProcessorStatisticDataHolder();
    private ConfigProvider configProvider;
    private MetricManagementService metricManagementService;
    private NodeInfo nodeInfo;
    private SiddhiAppRuntimeService siddhiAppRuntimeService;

    private StreamProcessorStatisticDataHolder() {
    }

    /**
     * Provide instance of StreamProcessorStatisticDataHolder class.
     *
     * @return Instance of StreamProcessorStatisticDataHolder
     */
    public static StreamProcessorStatisticDataHolder getInstance() {
        return instance;
    }
    /**
     * Returns servicers provider.
     *
     * @return Instance of servicers provider
     */
    public ConfigProvider getConfigProvider() {
        return this.configProvider;
    }

    /**
     * Sets instance of servicers provider.
     *
     * @param configProvider Instance of servicers provider
     */
    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Return the instance of  metrics configuration
     * @return metricsConfig Instance of Metrics Config
     */
    public MetricManagementService getMetricsManagementService() {
        return metricManagementService;
    }

    /**
     * Sets the instance of  metrics configuration
     */
    public void setMetricsManagementService(MetricManagementService metricManagementService) {
        this.metricManagementService = metricManagementService;
    }

    public NodeInfo getNodeInfo() {
        if(nodeInfo != null) {
            return nodeInfo;
        } else {
            String id = "";
            if(configProvider != null) {
                try {
                    id = configProvider.getConfigurationObject(CarbonConfiguration.class).getId();
                } catch (ConfigurationException e) {
                   logger.info("Error accessing servicers provider while getting HA details.");
                }
            }
            return null;
        }
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public SiddhiAppRuntimeService getSiddhiAppRuntimeService() {
        return siddhiAppRuntimeService;
    }

    public void setSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        this.siddhiAppRuntimeService = siddhiAppRuntimeService;
    }
}
