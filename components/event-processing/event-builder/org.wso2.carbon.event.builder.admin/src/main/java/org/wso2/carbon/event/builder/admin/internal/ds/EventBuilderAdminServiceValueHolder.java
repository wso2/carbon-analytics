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

package org.wso2.carbon.event.builder.admin.internal.ds;/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.wso2.carbon.event.builder.core.EventBuilderService;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;

public class EventBuilderAdminServiceValueHolder {

    private static EventBuilderService eventBuilderService;
    private static InputEventAdaptorManagerService inputEventAdaptorManagerService;
    private static InputEventAdaptorService inputEventAdaptorService;

    public static InputEventAdaptorService getInputEventAdaptorService() {
        return inputEventAdaptorService;
    }

    public static void registerInputEventAdaptorService(InputEventAdaptorService InputEventAdaptorService) {
        EventBuilderAdminServiceValueHolder.inputEventAdaptorService = InputEventAdaptorService;
    }

    public static void registerEventBuilderService(EventBuilderService eventBuilderService) {
        EventBuilderAdminServiceValueHolder.eventBuilderService = eventBuilderService;
    }

    public static EventBuilderService getEventBuilderService() {
        return EventBuilderAdminServiceValueHolder.eventBuilderService;
    }

    public static void registerInputEventAdaptorManagerService(
            InputEventAdaptorManagerService inputEventAdaptorManagerService) {
        EventBuilderAdminServiceValueHolder.inputEventAdaptorManagerService = inputEventAdaptorManagerService;
    }

    public static InputEventAdaptorManagerService getInputEventAdaptorManagerService() {
        return EventBuilderAdminServiceValueHolder.inputEventAdaptorManagerService;
    }
}
