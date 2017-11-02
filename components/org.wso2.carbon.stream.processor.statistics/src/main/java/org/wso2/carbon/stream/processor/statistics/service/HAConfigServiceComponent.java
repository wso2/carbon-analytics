/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.stream.processor.statistics.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.stream.processor.core.ha.HAInfo;
import org.wso2.carbon.stream.processor.statistics.internal.StreamProcessorStatisticDataHolder;


/**
 * Service component which is used to get the HA details of worker node.
 */
@Component(
        name = "org.wso2.carbon.stream.processor.statistics.service.HAConfigServiceComponent",
        service = HAConfigServiceComponent.class,
        immediate = true
)
public class HAConfigServiceComponent {

    public HAConfigServiceComponent() {
    }

    /**
     * Get the HAInfo service.
     * This is the bind method that gets called for HAInfo service registration that satisfy the policy.
     *
     * @param haInfo the HAInfo service that is registered as a service.
     */
    @Reference(
            name = "org.wso2.carbon.stream.processor.core.ha.HAInfo",
            service = HAInfo.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterHAInfoProvider"
    )
    protected void registerHAInfoProvider(HAInfo haInfo){
        StreamProcessorStatisticDataHolder.getInstance().setHaInfo(haInfo);
    }

    protected void unregisterHAInfoProvider(HAInfo haInfo){
        StreamProcessorStatisticDataHolder.getInstance().setHaInfo(null);
    }
}
