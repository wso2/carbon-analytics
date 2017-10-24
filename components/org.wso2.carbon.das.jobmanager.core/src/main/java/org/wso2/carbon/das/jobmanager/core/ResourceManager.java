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

package org.wso2.carbon.das.jobmanager.core;

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.model.Heartbeat;

public class ResourceManager implements HeartbeatListener {
    private static final Logger LOG = Logger.getLogger(ResourceManager.class);

    // TODO: 10/24/17 Managing/ Rebalancing things should go here

    @Override
    public void heartbeatAdded(Heartbeat heartbeat) {
        LOG.info("HeartbeatAdded: " + heartbeat);
    }

    @Override
    public void heartbeatUpdated(Heartbeat heartbeat) {
        LOG.info("HeartbeatUpdated: " + heartbeat);

    }

    @Override
    public void heartbeatExpired(Heartbeat heartbeat) {
        LOG.info("HeartbeatExpired: " + heartbeat);
        // TODO: 10/24/17 Handle Heartbeat
    }
}
