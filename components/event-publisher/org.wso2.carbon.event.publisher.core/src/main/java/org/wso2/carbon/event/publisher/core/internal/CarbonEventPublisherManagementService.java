/*
* Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.event.publisher.core.internal;


import org.wso2.carbon.event.processor.manager.core.EventPublisherManagementService;

public class CarbonEventPublisherManagementService extends EventPublisherManagementService {
    private boolean isDrop = false;

    public CarbonEventPublisherManagementService() {
    }

    @Override
    public boolean isDrop() {
        return isDrop;
    }

    @Override
    public void setDrop(boolean isDrop) {
        this.isDrop = isDrop;
    }

}
