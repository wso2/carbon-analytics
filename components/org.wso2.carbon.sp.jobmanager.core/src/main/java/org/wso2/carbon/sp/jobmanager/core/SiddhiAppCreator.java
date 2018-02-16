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

package org.wso2.carbon.sp.jobmanager.core;

import org.wso2.carbon.sp.jobmanager.core.appcreator.DeployableSiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopology;

import java.util.List;
/**
 * This interface is utilized by  which will be implemented by different
 * distributed
 * deployment implementations. Implementor can either choose to
 * implement from scratch using this interface or use.
 */
public interface SiddhiAppCreator {
    /**
     * Create valid concrete Siddhi Apps for each Query Group in the given {@link SiddhiTopology}
     *
     * @param topology Input topology to create Siddhi Apps
     * @return List of {@link DeployableSiddhiQueryGroup}s. Length of the list should be equal to no. of groups user
     * has defined. Length of the list should be greater than zero always.
     */
    List<DeployableSiddhiQueryGroup> createApps(SiddhiTopology topology);
}
