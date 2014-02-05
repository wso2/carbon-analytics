package org.wso2.carbon.event.input.adaptor.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.manager.admin.internal.util.InputEventAdaptorHolder;
import org.wso2.carbon.event.input.adaptor.manager.admin.internal.util.InputEventAdaptorManagerHolder;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * this class is used to get the EventAdaptorManager service.
 *
 * @scr.component name="input.event.adaptor.manager.admin.component" immediate="true"
 * @scr.reference name="input.event.adaptor.manager.service"
 * interface="org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorManagerService" unbind="unSetEventAdaptorManagerService"
 * @scr.reference name="input.event.adaptor.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorService" unbind="unSetEventAdaptorService"
 */
public class InputEventAdaptorManagerAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setEventAdaptorManagerService(
            InputEventAdaptorManagerService inputEventAdaptorManagerService) {
        InputEventAdaptorManagerHolder.getInstance().registerEventAdaptorManagerService(inputEventAdaptorManagerService);
    }

    protected void unSetEventAdaptorManagerService(
            InputEventAdaptorManagerService inputEventAdaptorManagerService) {
        InputEventAdaptorManagerHolder.getInstance().unRegisterEventAdaptorManagerService(inputEventAdaptorManagerService);
    }

    protected void setEventAdaptorService(InputEventAdaptorService inputEventAdaptorService) {
        InputEventAdaptorHolder.getInstance().registerEventService(inputEventAdaptorService);
    }

    protected void unSetEventAdaptorService(InputEventAdaptorService eventService) {
        InputEventAdaptorHolder.getInstance().unRegisterEventService(eventService);
    }
}
