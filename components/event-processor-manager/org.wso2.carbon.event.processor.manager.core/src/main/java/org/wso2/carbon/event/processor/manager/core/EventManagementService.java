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

package org.wso2.carbon.event.processor.manager.core;

import org.wso2.carbon.event.processor.manager.core.config.ManagementModeInfo;
import org.wso2.siddhi.core.event.Event;

public interface EventManagementService {

    public void subscribe(Manager manager);

    public ManagementModeInfo getManagementModeInfo();

    public void unsubscribe(Manager manager);

    public void syncEvent(String syncId, Manager.ManagerType type, Event event);

    public void registerEventSync(EventSync eventSync);

    public void unregisterEventSync(String syncId);


}
