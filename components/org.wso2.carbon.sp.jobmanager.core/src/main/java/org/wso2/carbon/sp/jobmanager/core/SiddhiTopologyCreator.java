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

import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiTopology;

/**
 * Topology Creator will consume a Siddhi App and produce a {@link SiddhiTopology} based on distributed annotations.
 * Implementation of this should not change depending on different distribution methodologies(Ex: default, yarn
 * based, container based).
 */
public interface SiddhiTopologyCreator {
    /**
     * consume a Siddhi App and produce a {@link SiddhiTopology} based on distributed annotations
     *
     * @param userDefinedSiddhiApp Siddhi app deployed by the user
     * @return {@link SiddhiTopology} representing the given siddhi app
     */
    SiddhiTopology createTopology(String userDefinedSiddhiApp);
}
