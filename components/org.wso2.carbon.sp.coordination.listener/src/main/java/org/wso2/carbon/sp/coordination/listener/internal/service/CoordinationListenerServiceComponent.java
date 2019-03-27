/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.sp.coordination.listener.internal.service;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.sp.coordination.listener.CoordinationEventListener;
import org.wso2.carbon.sp.coordination.listener.internal.CoordinationListenerDataHolder;

/**
 * Service component for getting the wso2 carbon metrics service.
 */
@Component(
        name = "org.wso2.carbon.sp.coordination.listener.internal.service.CoordinationListenerServiceComponent",
        service = CoordinationListenerServiceComponent.class,
        immediate = true
)
public class CoordinationListenerServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(CoordinationListenerServiceComponent.class);

    private boolean clusterComponentActivated;
    
    public CoordinationListenerServiceComponent() {
    }
    
    @Activate
    protected void start(BundleContext bundleContext) {
//        bundleContext.registerService(SPMetricsManagement.class.getName(), SPMetricsManagement.getInstance(),null);
        log.debug("CoordinationListenerServiceComponent has been activated.");
    }
    
    
    @Deactivate
    protected void stop() throws Exception {
        log.debug("CoordinationListenerServiceComponent has been stopped.");
    }


    @Reference(
            name = "org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator",
            service = ClusterCoordinator.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterClusterCoordinator"
    )
    protected void registerClusterCoordinator(ClusterCoordinator clusterCoordinator) throws ConfigurationException {
        if (clusterCoordinator != null) {
            clusterComponentActivated = true;
            CoordinationListenerDataHolder.setClusterCoordinator(clusterCoordinator);
            CoordinationListenerDataHolder.setIsLeader(clusterCoordinator.isLeaderNode());
            clusterCoordinator.registerEventListener(new CoordinationEventListener());
        }
    }

    protected void unregisterClusterCoordinator(ClusterCoordinator clusterCoordinator) {
        CoordinationListenerDataHolder.setClusterCoordinator(null);
    }
}
