package org.wso2.carbon.sp.jobmanager.core;

import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;

/**
 * This interface contains abstract method to get ResourceNode to deploy partial siddhi file.
 */
public interface ResourceAllocationAlgorithm {

    /**
     * Get next ResourceNode to deploy siddhi
     * @return Resource Node
     */
    ResourceNode getNextResourceNode();
}
