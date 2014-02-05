package org.wso2.carbon.event.formatter.admin.internal.util;/*
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


import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;


public class EventFormatterAdminServiceValueHolder {

    private static OutputEventAdaptorManagerService outputEventAdaptorManagerService;
    private static OutputEventAdaptorService outputEventAdaptorService;
    private static EventFormatterService eventFormatterService;

    public static void registerEventAdaptorManagerService(
            OutputEventAdaptorManagerService eventAdaptorManagerService) {
        EventFormatterAdminServiceValueHolder.outputEventAdaptorManagerService = eventAdaptorManagerService;
    }

    public static OutputEventAdaptorManagerService getOutputEventAdaptorManagerService() {
        return EventFormatterAdminServiceValueHolder.outputEventAdaptorManagerService;
    }

    public static void registerEventAdaptorService(
            OutputEventAdaptorService eventAdaptorService) {
        EventFormatterAdminServiceValueHolder.outputEventAdaptorService = eventAdaptorService;
    }

    public static OutputEventAdaptorService getOutputEventAdaptorService() {
        return EventFormatterAdminServiceValueHolder.outputEventAdaptorService;
    }

    public static void registerEventFormatterService(
            EventFormatterService eventFormatterService) {
        EventFormatterAdminServiceValueHolder.eventFormatterService = eventFormatterService;
    }

    public static EventFormatterService getEventFormatterService() {
        return EventFormatterAdminServiceValueHolder.eventFormatterService;
    }
}
