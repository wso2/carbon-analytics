/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.receiver.admin.internal.ds;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.receiver.core.EventReceiverService;


public class EventReceiverAdminServiceValueHolder {

    private static InputEventAdapterService inputEventAdapterService;
    private static EventReceiverService eventReceiverService;
    
    public static void registerEventAdapterService(
            InputEventAdapterService eventAdapterService) {
        EventReceiverAdminServiceValueHolder.inputEventAdapterService = eventAdapterService;
    }

    public static InputEventAdapterService getInputEventAdapterService() {
        return EventReceiverAdminServiceValueHolder.inputEventAdapterService;
    }

    public static void registerEventReceiverService(
            EventReceiverService eventReceiverService) {
        EventReceiverAdminServiceValueHolder.eventReceiverService = eventReceiverService;
    }

    public static EventReceiverService getEventReceiverService() {
        return EventReceiverAdminServiceValueHolder.eventReceiverService;
    }
}
