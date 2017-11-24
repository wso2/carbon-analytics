package org.wso2.carbon.das.yarn.core.impl;

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.DeploymentManager;
import org.wso2.carbon.das.jobmanager.core.SiddhiAppCreator;
import org.wso2.carbon.das.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.das.jobmanager.core.appCreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopology;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopologyCreatorImpl;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;

import java.util.List;

public class YarnDistributionServiceImpl implements DistributionService {
    private SiddhiAppCreator appCreator;
    private DeploymentManager deploymentManager;
    private SiddhiTopologyCreator siddhiTopologyCreator;
    private final Logger LOG = Logger.getLogger(YarnDistributionServiceImpl.class);

    public YarnDistributionServiceImpl(SiddhiAppCreator appCreator, DeploymentManager deploymentManager) {
        LOG.info("************************initializing "+ this.getClass().getName());
        this.appCreator = appCreator;
        this.deploymentManager = deploymentManager;
        siddhiTopologyCreator = new SiddhiTopologyCreatorImpl();
    }

    @Override public DeploymentStatus distribute(String userDefinedSiddhiApp) {
        SiddhiTopology topology = siddhiTopologyCreator.createTopology(userDefinedSiddhiApp);
        List<DeployableSiddhiQueryGroup> deployableQueryGroup = appCreator.createApps(topology);
        return deploymentManager.deploy(new DistributedSiddhiQuery(topology.getName(), deployableQueryGroup));
    }

    @Override public RuntimeMode getRuntimeMode() {
        return RuntimeMode.MANAGER;    }

    @Override public DeploymentMode getDeploymentMode() {
        return ServiceDataHolder.getDeploymentMode();
    }

    @Override public boolean isDistributed(String parentSiddhiAppName) {
        return deploymentManager.isDeployed(parentSiddhiAppName);
    }

    @Override public void undeploy(String parentSiddhiAppName) {
        deploymentManager.unDeploy(parentSiddhiAppName);
    }

    @Override public boolean isLeader() {
        return false;
    }
}
