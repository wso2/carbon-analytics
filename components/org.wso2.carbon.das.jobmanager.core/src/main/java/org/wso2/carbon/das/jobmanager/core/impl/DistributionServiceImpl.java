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

package org.wso2.carbon.das.jobmanager.core.impl;

import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.DistributionService;
import org.wso2.carbon.das.jobmanager.core.SiddhiAppCreator;
import org.wso2.carbon.das.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.deployment.DeploymentStatus;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopologyCreatorImpl;

import java.util.List;

/**
 * Abstract implementation of {@link DistributionService}. This implementation mandate to use an
 * {@link SiddhiAppCreator}  and {@link DeploymentManager} to fulfill distribution of Siddhi App.
 */
public class DistributionServiceImpl implements DistributionService {
    private SiddhiAppCreator appCreator;
    private DeploymentManager deploymentManager;
    private SiddhiTopologyCreator siddhiTopologyCreator;

    private DistributionServiceImpl() {
        //Do nothing
    }

    public DistributionServiceImpl(SiddhiAppCreator appCreator, DeploymentManager deploymentManager) {
        this.appCreator = appCreator;
        this.deploymentManager = deploymentManager;
        siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
    }

    public DeploymentStatus distribute(String userDefinedSiddhiApp) {
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(userDefinedSiddhiApp);
        List<DeployableSiddhiQueryGroup> deployableQueryGroup = appCreator.createApps(topology);
        return deploymentManager.deploy(deployableQueryGroup);
    }

}
