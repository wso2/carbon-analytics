/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;


/**
 * This class is used to get the EventBuilder service.
 *
 * @scr.component name="eventBuilderAdmin.component" immediate="true"
 * @scr.reference name="eventBuilderService.service"
 * interface="org.wso2.carbon.event.builder.core.EventBuilderService" cardinality="1..1"
 * policy="dynamic" bind="setEventBuilderService" unbind="unsetEventBuilderService"
 * @scr.reference name="inputEventAdaptorManager.service"
 * interface="org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorManagerService" unbind="unsetInputEventAdaptorManagerService"
 * @scr.reference name="inputEventAdaptor.service"
 * interface="org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setInputEventAdaptorService" unbind="unsetInputEventAdaptorService"
 */
public class EventBuilderAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setEventBuilderService(EventBuilderService eventBuilderService) {
        EventBuilderAdminServiceValueHolder.registerEventBuilderService(eventBuilderService);
    }

    protected void unsetEventBuilderService(EventBuilderService eventBuilderService) {
        EventBuilderAdminServiceValueHolder.registerEventBuilderService(null);
    }

    protected void setInputEventAdaptorManagerService(
            InputEventAdaptorManagerService transportManagerService) {
        EventBuilderAdminServiceValueHolder.registerInputEventAdaptorManagerService(transportManagerService);

    }

    protected void unsetInputEventAdaptorManagerService(
            InputEventAdaptorManagerService transportManagerService) {
        EventBuilderAdminServiceValueHolder.registerInputEventAdaptorManagerService(null);

    }

    protected void setInputEventAdaptorService(InputEventAdaptorService InputEventAdaptorService) {
        EventBuilderAdminServiceValueHolder.registerInputEventAdaptorService(InputEventAdaptorService);
    }

    protected void unsetInputEventAdaptorService(InputEventAdaptorService InputEventAdaptorService) {
        EventBuilderAdminServiceValueHolder.registerInputEventAdaptorService(null);
    }
}
