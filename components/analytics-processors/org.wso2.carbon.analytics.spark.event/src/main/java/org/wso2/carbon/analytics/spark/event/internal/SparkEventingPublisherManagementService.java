/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.spark.event.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;

/**
 * {@link EventPublisherManagementService} implementation to check whether the current node is
 * the active node or not.
 */
public class SparkEventingPublisherManagementService extends EventPublisherManagementService {

    private static final Log log = LogFactory.getLog(SparkEventingPublisherManagementService.class);

    private boolean drop = false;

    public SparkEventingPublisherManagementService() { }

    @Override
    public boolean isDrop() {
        return drop;
    }

    @Override
    public void setDrop(boolean drop) {
        this.drop = drop;
        if (log.isDebugEnabled()) {
            if (this.drop) {
                log.debug("This node configured for dropping events");
            } else {
                log.debug("This node configured for accepting events");
            }
        }
    }
    
}
