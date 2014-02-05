package org.wso2.carbon.event.output.adaptor.manager.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.manager.admin.internal.util.OutputEventAdaptorHolder;
import org.wso2.carbon.event.output.adaptor.manager.admin.internal.util.OutputEventAdaptorManagerHolder;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;


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
 * @scr.component name="output.event.adaptor.manager.admin.component" immediate="true"
 * @scr.reference name="output.event.adaptor.manager.service"
 * interface="org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorManagerService" unbind="unSetEventAdaptorManagerService"
 * @scr.reference name="output.event.adaptor.service"
 * interface="org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorService" unbind="unSetEventAdaptorService"
 */
public class OutputEventAdaptorManagerAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setEventAdaptorManagerService(
            OutputEventAdaptorManagerService outputEventAdaptorManagerService) {
        OutputEventAdaptorManagerHolder.getInstance().registerEventAdaptorManagerService(outputEventAdaptorManagerService);
    }

    protected void unSetEventAdaptorManagerService(
            OutputEventAdaptorManagerService outputEventAdaptorManagerService) {
        OutputEventAdaptorManagerHolder.getInstance().unRegisterEventAdaptorManagerService(outputEventAdaptorManagerService);
    }

    protected void setEventAdaptorService(
            OutputEventAdaptorService outputEventAdaptorService) {
        OutputEventAdaptorHolder.getInstance().registerEventService(outputEventAdaptorService);
    }

    protected void unSetEventAdaptorService(OutputEventAdaptorService eventAdaptorService) {
        OutputEventAdaptorHolder.getInstance().unRegisterEventService(eventAdaptorService);
    }
}
