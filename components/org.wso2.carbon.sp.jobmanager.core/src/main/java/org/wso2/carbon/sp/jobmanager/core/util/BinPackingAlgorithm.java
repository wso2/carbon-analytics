package org.wso2.carbon.sp.jobmanager.core.util;

import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.ResourceAllocationAlgorithm;
import org.wso2.carbon.sp.jobmanager.core.bean.DeploymentConfig;
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;
import org.wso2.carbon.sp.jobmanager.core.model.ResourcePool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class BinPackingAlgorithm implements ResourceAllocationAlgorithm {
    private static final Logger logger = Logger.getLogger(BinPackingAlgorithm.class);

    @Override
    public ResourceNode getNextResourceNode() {
        DeploymentConfig deploymentConfig = ServiceDataHolder.getDeploymentConfig();
        ResourcePool resourcePool = ServiceDataHolder.getResourcePool();
        if (deploymentConfig != null && resourcePool != null) {
            if (resourcePool.getResourceNodeMap().size() >= deploymentConfig.getMinResourceCount()) {
                Iterator resourceIterator = resourcePool.getResourceNodeMap().values().iterator();
                return sortResourcePool(resourceIterator);
            } else {
                logger.error("Minimum resource requirement did not match, hence not deploying the partial siddhi app ");
            }
        }
        return null;
    }

    private ResourceNode sortResourcePool(Iterator resourceIterator) {
        Map<String, Double> unsortedMap = new HashMap<>();
        while (resourceIterator.hasNext()) {
            ResourceNode resourceNode = (ResourceNode) resourceIterator.next();
            if (resourceNode.isMetricsUpdated()) {
                unsortedMap.put(resourceNode.getId(), resourceNode.getLoadAverage());
            } else {
                throw new ResourceManagerException("Metrics needs to be enabled on Resource node: "
                        + resourceNode.getId() + " to be used with Allocation algorithm class: "
                        + ServiceDataHolder.getAllocationAlgorithm().getClass().getCanonicalName());
            }
        }
        Map result = unsortedMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        String name = result.keySet().iterator().next().toString();
        return ServiceDataHolder.getResourcePool().getResourceNodeMap().get(name);
    }
}
