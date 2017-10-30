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

import org.wso2.carbon.das.jobmanager.core.appCreator.DistributedSiddhiQuery;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;

public interface DeploymentManager {
    /**
     * Deploy a distributed Siddhi app
     *
     * @param distributedSiddhiQuery distributed Siddhi app
     * @return the deployment status of the Siddhi app
     */
    DeploymentStatus deploy(DistributedSiddhiQuery distributedSiddhiQuery);

    /**
     * Un-deploy a distributed Siddhi app
     *
     * @param distributedSiddhiQuery distributed Siddhi app
     * @return the deployment status of the Siddhi app
     */
    DeploymentStatus unDeploy(DistributedSiddhiQuery distributedSiddhiQuery);

    /**
     * Get the deployment status of a distributed Siddhi app
     *
     * @param distributedSiddhiQuery distributed Siddhi app
     * @return 1: if the same app is already deployed,
     * 0: if the app is not already deployed,
     * -1: if a different app with the same name already deployed
     */
    // TODO: 10/30/17 Better to use a Enum within the DeploymentStatus and return it
    int isDeployed(DistributedSiddhiQuery distributedSiddhiQuery);
}
