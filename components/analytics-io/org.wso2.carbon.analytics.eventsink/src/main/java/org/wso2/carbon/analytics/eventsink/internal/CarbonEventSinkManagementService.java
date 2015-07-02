/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.eventsink.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;

public class CarbonEventSinkManagementService extends EventPublisherManagementService {
    private static final Log log = LogFactory.getLog(CarbonEventSinkManagementService.class);

    private boolean isDrop = false;

    public CarbonEventSinkManagementService() {

    }

    @Override
    public boolean isDrop() {
        return isDrop;
    }

    @Override
    public void setDrop(boolean isDrop) {
        if (isDrop != this.isDrop){
            if (isDrop) {
                log.info("This node will not the leader, and hence it will drop the events received by the node.");
            }else {
                log.info("This has become the leader, hence this will be storing the incoming events.");
            }
        }
        this.isDrop = isDrop;
    }
}
