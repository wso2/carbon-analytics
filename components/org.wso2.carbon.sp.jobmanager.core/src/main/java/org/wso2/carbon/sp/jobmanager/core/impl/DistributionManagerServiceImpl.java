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

package org.wso2.carbon.sp.jobmanager.core.impl;

import org.wso2.carbon.sp.jobmanager.core.DeploymentManager;
import org.wso2.carbon.sp.jobmanager.core.SiddhiAppCreator;
import org.wso2.carbon.sp.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.appcreator.DistributedSiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of {@link DistributionService}. This implementation mandate to use an
 * {@link SiddhiAppCreator}  and {@link DeploymentManager} to fulfill distribution of Siddhi App.
 */
public class DistributionManagerServiceImpl implements DistributionService {

    private SiddhiAppCreator appCreator;
    private DeploymentManager deploymentManager;
    private SiddhiTopologyCreator siddhiTopologyCreator;
    private Map<String, String> serviceHolder = new HashMap<>();

    private DistributionManagerServiceImpl() {
        //Do nothing
    }

    public DistributionManagerServiceImpl(SiddhiAppCreator appCreator, DeploymentManager deploymentManager) {
        this.appCreator = appCreator;
        this.deploymentManager = deploymentManager;
        siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
    }

    @Override
    public DeploymentStatus distribute(String userDefinedSiddhiApp) {
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(userDefinedSiddhiApp);
        List<DeployableSiddhiQueryGroup> deployableQueryGroupList = appCreator.createApps(topology);
        serviceHolder.put(topology.getName(), userDefinedSiddhiApp);
        ServiceDataHolder.setUserDefinedSiddhiApp(serviceHolder);
        return deploymentManager.deploy(new DistributedSiddhiQuery(topology.getName(), deployableQueryGroupList));
    }

    @Override
    public RuntimeMode getRuntimeMode() {
        return RuntimeMode.MANAGER;
    }

    @Override
    public DeploymentMode getDeploymentMode() {
        return ServiceDataHolder.getDeploymentMode();
    }

    @Override
    public boolean isDistributed(String parentSiddhiAppName) {
        return deploymentManager.isDeployed(parentSiddhiAppName);
    }

    @Override
    public void undeploy(String parentSiddhiAppName) {
        deploymentManager.unDeploy(parentSiddhiAppName);
    }

    @Override
    public boolean isLeader() {
        // coordinatorChanged method of CoordinatorChangeListener might take some
        // time to get triggered depending on cluster coordinator ref resolve time.
        // until then, this will wait in a busy loop.
        while (ServiceDataHolder.getLeaderNode() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        return ServiceDataHolder.isLeader();
    }
}
