/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.stream.processor.core.ha.ActiveNodeOutputSyncManager;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationRecordTableHandlerManager;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandlerManager;
import org.wso2.carbon.stream.processor.core.ha.HAManager;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.DeploymentConfig;
import org.wso2.carbon.stream.processor.core.internal.beans.LiveSyncConfig;
import org.wso2.siddhi.core.SiddhiManager;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(StreamProcessorDataHolder.class)
public class HAManagerTest extends PowerMockTestCase {

    @Test
    public void testHaManager() {

        ClusterCoordinator clusterCoordinator = mock(ClusterCoordinator.class);
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("host", "localhost");
        propertiesMap.put("host", "9090");
        when(clusterCoordinator.getLeaderNode()).thenReturn(new NodeDetail("wso2-sp", "group-1", true, 1L,
                true, propertiesMap));
        mockStatic(StreamProcessorDataHolder.class);
        when(StreamProcessorDataHolder.getSiddhiManager()).thenReturn(new SiddhiManager());
        when(StreamProcessorDataHolder.getNodeInfo()).thenReturn(new NodeInfo(DeploymentMode.MINIMUM_HA, "wso2-sp"));

        DeploymentConfig deploymentConfig = new DeploymentConfig();
        deploymentConfig.setType("ha");
        LiveSyncConfig liveSyncConfig = new LiveSyncConfig();
        liveSyncConfig.setEnabled(false);
        deploymentConfig.setLiveSync(liveSyncConfig);

        HAManager haManager = new HAManager(clusterCoordinator, "wso2-sp", "group-1", deploymentConfig);
        haManager.start();
    }
}
