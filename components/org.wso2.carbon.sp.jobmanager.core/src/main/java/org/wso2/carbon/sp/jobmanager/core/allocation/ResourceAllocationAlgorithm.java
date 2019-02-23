/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sp.jobmanager.core.allocation;

import org.wso2.carbon.sp.jobmanager.core.appcreator.SiddhiQuery;
import org.wso2.carbon.sp.jobmanager.core.model.ResourceNode;

import java.util.Map;

/**
 * This interface contains abstract method to get ResourceNode to deploy partial siddhi file.
 */
public interface ResourceAllocationAlgorithm {

    /**
     * Get next ResourceNode to deploy siddhi
     * @param resourceNodeMap ResourceNode Map
     * @param minResourceCount Minimum resource requirement for SiddhiQuery
     * @return Elected resource node for next deployment
     */
    ResourceNode getNextResourceNode(Map<String, ResourceNode> resourceNodeMap, int minResourceCount,
                                     SiddhiQuery siddhiQuery);
}
