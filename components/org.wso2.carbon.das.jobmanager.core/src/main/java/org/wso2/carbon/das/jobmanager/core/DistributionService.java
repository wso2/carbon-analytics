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

package org.wso2.carbon.das.jobmanager.core;/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.wso2.carbon.das.jobmanager.core.deployment.DeploymentStatus;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiTopology;

/**
 * Parent interface of Distribution Service. Abstract implementation of this is {@link AbstractDistributionService}.
 * Developers are expected to use that extension point.
 */
public interface DistributionService {
    /**
     * Distribute the given {@link SiddhiTopology} into available resources as governed by the underlying
     * implementation.
     *
     * @param topology Topology representing the user-defined distributed Siddhi App
     * @return Status of the deployment including node connection details to edge nodes with which user will
     * collaborate.
     */
    DeploymentStatus distribute(SiddhiTopology topology);
}
