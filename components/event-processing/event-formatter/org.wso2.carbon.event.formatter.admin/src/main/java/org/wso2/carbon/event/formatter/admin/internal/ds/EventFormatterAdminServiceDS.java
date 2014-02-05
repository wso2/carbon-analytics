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

package org.wso2.carbon.event.formatter.admin.internal.ds;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.formatter.admin.internal.util.EventFormatterAdminServiceValueHolder;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;

/**
 * This class is used to get the EventFormatter service.
 *
 * @scr.component name="eventFormatterAdmin.component" immediate="true"
 * @scr.reference name="eventmanager.service"
 * interface="org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorManagerService" unbind="unSetEventAdaptorManagerService"
 * @scr.reference name="event.adaptor.service"
 * interface="org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService" cardinality="1..1"
 * policy="dynamic" bind="setEventAdaptorService" unbind="unSetEventAdaptorService"
 * @scr.reference name="eventFormatterService.service"
 * interface="org.wso2.carbon.event.formatter.core.EventFormatterService" cardinality="1..1"
 * policy="dynamic" bind="setEventFormatterService" unbind="unSetEventFormatterService"
 */
public class EventFormatterAdminServiceDS {
    protected void activate(ComponentContext context) {

    }

    protected void setEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventAdaptorManagerService) {
        EventFormatterAdminServiceValueHolder.registerEventAdaptorManagerService(eventAdaptorManagerService);
    }

    protected void unSetEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventManagerService) {

    }

    protected void setEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventFormatterAdminServiceValueHolder.registerEventAdaptorService(eventAdaptorService);
    }

    protected void unSetEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {

    }

    protected void setEventFormatterService(EventFormatterService eventFormatterService) {
        EventFormatterAdminServiceValueHolder.registerEventFormatterService(eventFormatterService);
    }

    protected void unSetEventFormatterService(EventFormatterService eventFormatterService) {

    }


}
