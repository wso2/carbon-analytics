package org.wso2.carbon.sp.jobmanager.core.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.ResourceAllocationAlgorithm;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;

import java.util.Iterator;

/**
 *
 */
public class RoundRobinAllocationAlgorithm implements ResourceAllocationAlgorithm {
    private static final Logger logger = Logger.getLogger(RoundRobinAllocationAlgorithm.class);
    private Iterator resourceIterator;

    @Override
    public ResourceNode getNextResourceNode() {
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        if (deploymentConfig != null && resourcePool != null) {
            if (resourcePool.getResourceNodeMap().size() >= deploymentConfig.getMinResourceCount()) {
                if (resourceIterator == null) {
                    resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
                }
                if (resourceIterator.hasNext()) {
                    return (ResourceNode) resourceIterator.next();
                } else {
                    resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
                    if (resourceIterator.hasNext()) {
                        return (ResourceNode) resourceIterator.next();
                    }
                }
            } else {
                logger.error("Minimum resource requirement did not match, hence not deploying the partial siddhi app ");
            }
        }
        return null;
    }
}
