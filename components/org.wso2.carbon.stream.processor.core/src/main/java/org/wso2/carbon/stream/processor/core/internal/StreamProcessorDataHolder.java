/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.internal;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.stream.processor.core.coordination.HACoordinationSinkHandlerManager;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.output.sink.SinkHandlerManager;

/**
 * Class which holds the OSGI Service references
 */
public class StreamProcessorDataHolder {

    private static StreamProcessorDataHolder instance = new StreamProcessorDataHolder();
    private static SiddhiManager siddhiManager;
    private static StreamProcessorService streamProcessorService;
    private DataSourceService dataSourceService;
    private boolean isPersistenceEnabled;
    private ClusterCoordinator clusterCoordinator;
    private CarbonRuntime carbonRuntime;
    private SiddhiAppProcessorConstants.RuntimeMode runtimeMode = SiddhiAppProcessorConstants.RuntimeMode.ERROR;
    private BundleContext bundleContext;
    private ConfigProvider configProvider;
    private SinkHandlerManager sinkHandlerManager;

    private StreamProcessorDataHolder() {

    }

    /**
     * This returns the StreamProcessorDataHolder instance.
     *
     * @return The StreamProcessorDataHolder instance of this singleton class
     */
    public static StreamProcessorDataHolder getInstance() {
        return instance;
    }

    public static SiddhiManager getSiddhiManager() {
        return siddhiManager;
    }

    public static void setSiddhiManager(SiddhiManager siddhiManager) {
        StreamProcessorDataHolder.siddhiManager = siddhiManager;
    }

    public static StreamProcessorService getStreamProcessorService() {
        return streamProcessorService;
    }

    public static void setStreamProcessorService(StreamProcessorService streamProcessorService) {
        StreamProcessorDataHolder.streamProcessorService = streamProcessorService;
    }

    public DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public boolean isPersistenceEnabled() {
        return isPersistenceEnabled;
    }

    public void setIsPersistenceEnabled(Boolean isPersistenceEnabled) {
        this.isPersistenceEnabled = isPersistenceEnabled;
    }

    public ClusterCoordinator getClusterCoordinator() {
        return clusterCoordinator;
    }

    public void setClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        this.clusterCoordinator = clusterCoordinator;
    }

    public SinkHandlerManager getSinkHandlerManager() {
        return sinkHandlerManager;
    }

    public void setSinkHandlerManager(SinkHandlerManager sinkHandlerManager) {
        this.sinkHandlerManager = sinkHandlerManager;
    }

    /**
     * Returns the CarbonRuntime service which gets set through a service component.
     *
     * @return CarbonRuntime Service
     */
    public CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    /**
     * This method is for setting the CarbonRuntime service. This method is used by
     * ServiceComponent.
     *
     * @param carbonRuntime The reference being passed through ServiceComponent
     */
    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
    }

    public SiddhiAppProcessorConstants.RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public void setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }
}
